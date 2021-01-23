package com.relino.core.task;

import com.relino.core.exception.HandleException;
import com.relino.core.model.JobStatus;
import com.relino.core.model.db.ExecuteTimeEntity;
import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.Utils;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.DBPessimisticLock;
import com.relino.core.support.db.KVStore;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 扫描DeadJob，让其重新参加运行
 * DeadJob条件为：当执行后超过指定时间仍然处于{@link JobStatus#RUNNABLE}状态的Job
 *
 * @author kaiqiang.he
 */
public class DeadJobWatchDog extends AbstractRunSupport implements ElectionCandidate {

    private static final Logger log = LoggerFactory.getLogger(DeadJobWatchDog.class);

    private static final String DEAD_JOB_WATCH_DOG_KEY = "dead_job_watch_dog";

    public static final int MIN_DEAD_JOB_MINUTES = 5;

    private DBExecutor dbExecutor;
    private KVStore kvStore;
    private DBPessimisticLock dbPessimisticLock;
    private int deadJobMinute;

    /**
     * @param dbExecutor not null
     * @param deadJobMinute >= {@link MIN_DEAD_JOB_MINUTES}
     */
    public DeadJobWatchDog(DBExecutor dbExecutor, int deadJobMinute) {
        Utils.checkNoNull(dbExecutor);
        Utils.check(deadJobMinute, v -> v < MIN_DEAD_JOB_MINUTES, "deadJobMinute应该>=" + deadJobMinute + ", current = " + deadJobMinute);

        this.deadJobMinute = deadJobMinute;
        this.dbExecutor = dbExecutor;
        this.kvStore = new KVStore(dbExecutor);
        this.dbPessimisticLock = new DBPessimisticLock(dbExecutor);
    }

    @Override
    protected void execute0() throws InterruptedException {
        log.info("DeadJobWatchDog开始运行");
        while (true) {

            boolean sleep;
            try {
                if(wannaStop()) { break; }
                sleep = doWatchDeadJob();
                if(wannaStop()) { break; }
            } catch (Throwable t) {
                sleep = true;
                HandleException.handleUnExpectedException(t);
            }

            /**
             * 注意：
             * 如果doScan()操作异常，这里一定要Sleep
             */
            if(sleep) {
                Thread.sleep(10000);
            }
        }
        log.info("DeadJobWatchDog运行结束");
    }

    private boolean doWatchDeadJob() throws SQLException {
        boolean sleep = true;
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(deadJobMinute);
        long start = System.currentTimeMillis();
        try {
            dbExecutor.beginTx();
            dbPessimisticLock.openPessimisticLock(DEAD_JOB_WATCH_DOG_KEY);
            long lastExecuteOrder = Long.parseLong(kvStore.get(DEAD_JOB_WATCH_DOG_KEY));
            ExecuteTimeEntity entity = getNextExecuteTimeLog(startTime);
            if(entity != null) {
                long executeOrder = entity.getExecuteOrder();
                List<Long> deadJobs = getDeadJobIds(lastExecuteOrder, executeOrder);
                if(!Utils.isEmpty(deadJobs)) {
                    setDeadJobAvailable(deadJobs, LocalDateTime.now());
                }

                kvStore.update(DEAD_JOB_WATCH_DOG_KEY, Long.toString(entity.getExecuteOrder()));
                deleteExecuteTimeRecord(entity.getId());
                sleep = false;

                if(log.isDebugEnabled()) {
                    log.debug("ScanRunnableDelayJob, ids = [{}], time = {}",
                            deadJobs.stream().map(v -> v + "").collect(Collectors.joining(",")),
                            System.currentTimeMillis() - start);
                }
            }

            dbExecutor.commitTx();
        } catch (Throwable t) {
            dbExecutor.rollbackTx();
            HandleException.handleUnExpectedException(t);
            sleep = true;
        }
        return sleep;
    }

    @Override
    public void executeWhenCandidate() throws Exception {
        execute();
    }

    @Override
    public void stopExecute() {
        terminal();
    }

    // ----------------------------------------------------------------------------------
    // store
    public int deleteExecuteTimeRecord(long id) throws SQLException {
        String sql = "delete from execute_time where id = ?";
        return dbExecutor.execute(sql, new Object[]{id});
    }

    /**
     * 查询处于(startExecuteOrder, endExecuteOrder] 且 jobStatus = 2
     *
     * @return not null
     */
    private List<Long> getDeadJobIds(long startExecuteOrder, long endExecuteOrder) throws SQLException {
        String sql = "select id from job where job_status = " + JobStatus.RUNNABLE.getCode() + " and execute_order > ? and execute_order <= ?";
        List<BigInteger> ids = dbExecutor.query(sql, new ColumnListHandler<>("id"), new Object[]{startExecuteOrder, endExecuteOrder});
        if(Utils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return ids.stream().map(BigInteger::longValue).collect(Collectors.toList());
        }
    }

    private int setDeadJobAvailable(List<Long> ids, LocalDateTime willExecuteTime) throws SQLException {
        if(Utils.isEmpty(ids)) {
            return 0;
        }

        int idSize = ids.size();
        String sql = "update job set job_status = " + JobStatus.DELAY.getCode() + ", will_execute_time = ? where id in " + Utils.getNQuestionMark(idSize);
        Object[] param = new Object[idSize + 1];
        param[0] = Utils.toStrDate(willExecuteTime);
        for (int i = 0; i < idSize; i++) {
            param[i + 1] = ids.get(i);
        }
        return dbExecutor.execute(sql, param);
    }

    private ExecuteTimeEntity getNextExecuteTimeLog(LocalDateTime time) throws SQLException {
        String sql = "select id, execute_order, execute_job_time from execute_time where execute_job_time <= ? order by id limit 1";
        Map<String, Object> row = dbExecutor.query(sql, new MapHandler(), new Object[]{Utils.toStrDate(time)});
        if(row == null) {
            return null;
        }

        ExecuteTimeEntity ret = new ExecuteTimeEntity();
        ret.setId(((BigInteger) row.get("id")).longValue());
        ret.setExecuteOrder(((long) row.get("execute_order")));
        ret.setExecuteJobTime(Utils.toLocalDateTime((Date) row.get("execute_job_time")));
        return ret;
    }
}
