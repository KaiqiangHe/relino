package com.relino.core.task;

import com.relino.core.exception.HandleException;
import com.relino.core.model.JobStatus;
import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.Utils;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.DBPessimisticLock;
import com.relino.core.support.db.KVStore;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扫描可以运行的job
 *
 * @author kaiqiang.he
 */
public final class ScanRunnableDelayJob implements ElectionCandidate {

    private static final Logger log = LoggerFactory.getLogger(ScanRunnableDelayJob.class);

    public static final int MAX_BATCH_SIZE = 400;

    private static final String EXECUTE_ORDER_KEY = "execute_order";
    private static final int TIME_STEP = 10;

    private DBExecutor dbExecutor;
    private KVStore kvStore;
    private DBPessimisticLock dbPessimisticLock;
    private int batchSize;

    /**
     * @param dbExecutor not null
     * @param batchSize 属于[1, {@link MAX_BATCH_SIZE}]
     */
    public ScanRunnableDelayJob(DBExecutor dbExecutor, int batchSize) {

        Utils.checkNoNull(dbExecutor);
        Utils.check(batchSize, v -> v < 1 || v > MAX_BATCH_SIZE, "batchSize应该属于[1, " + MAX_BATCH_SIZE + "], current = " + batchSize);

        this.dbExecutor = dbExecutor;
        this.kvStore = new KVStore(dbExecutor);
        this.dbPessimisticLock = new DBPessimisticLock(dbExecutor);
        this.batchSize = batchSize;
    }

    @Override
    public void executeWhenCandidate() throws InterruptedException {
        while (!Thread.interrupted()) {
            boolean sleep;
            try {
                sleep = doScan();
            } catch (InterruptedException e) {
                throw e;
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
                sleep = true;
            }

            if(sleep) {
                Thread.sleep(200);
            }
        }
    }

    @Override
    public void destroy() {
        // empty
    }

    /**
     * 返回是否要sleep
     */
    public boolean doScan() throws Exception {

        LocalDateTime now = LocalDateTime.now();
        long start = System.currentTimeMillis();
        return dbExecutor.executeWithTx(none -> {
            dbPessimisticLock.openPessimisticLock(EXECUTE_ORDER_KEY);
            String value = kvStore.get(EXECUTE_ORDER_KEY);
            long executeOrder = Long.parseLong(value);
            List<Long> ids = getNextCanRunnableJobIds(now.minusMinutes(TIME_STEP), now, batchSize);
            if(ids.isEmpty()) {
                return true;
            }

            // 更新可执行job
            List<IdAndExecuteOrder> updateData = new ArrayList<>();
            for (Long id : ids) {
                executeOrder++;
                updateData.add(new IdAndExecuteOrder(id, executeOrder));
            }
            setSleepJobRunnable(updateData);
            // 更新executeOrder
            kvStore.update(EXECUTE_ORDER_KEY, Long.toString(executeOrder));
            if(log.isDebugEnabled()) {
                log.debug("ScanRunnableDelayJob, ids = [{}], time = {}",
                        ids.stream().map(v -> v + "").collect(Collectors.joining(",")),
                        System.currentTimeMillis() - start);
            }
            return false;
        }, null);
    }

    /**
     * 获取下一批有sleep转为runnable的job
     * 返回willExecuteTime在[start, end] 且 jobStatus为{@link JobStatus#DELAY} 的limit个job
     *
     * @return not null, 如果无数据返回空list
     */
    public List<Long> getNextCanRunnableJobIds(LocalDateTime start, LocalDateTime end, int limit) throws SQLException {
        String sql = "select id from job where job_status = " + JobStatus.DELAY.getCode() + " and will_execute_time >= ? and will_execute_time <= ? order by will_execute_time limit ?";
        Object[] params = new Object[]{Utils.toStrDate(start), Utils.toStrDate(end), limit};
        List<BigInteger> ids = dbExecutor.query(sql, new ColumnListHandler<>("id"), params);
        if(Utils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return ids.stream().map(BigInteger::longValue).collect(Collectors.toList());
        }
    }

    public void setSleepJobRunnable(List<IdAndExecuteOrder> elems) throws SQLException {

        if(elems == null || elems.isEmpty()) {
            return ;
        }

        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("update job set job_status = ?, execute_order = ");
        params.add(JobStatus.RUNNABLE.getCode());
        sb.append("case id ");
        elems.forEach(v -> {
            sb.append("when ? then ? ");
            params.add(v.getId());
            params.add(v.getExecuteOrder());
        });
        sb.append("end ");
        sb.append("where id in ").append(Utils.getNQuestionMark(elems.size()));
        List<Long> ids = elems.stream().map(IdAndExecuteOrder::getId).collect(Collectors.toList());
        params.addAll(ids);

        dbExecutor.execute(sb.toString(), params.toArray());
    }
}
