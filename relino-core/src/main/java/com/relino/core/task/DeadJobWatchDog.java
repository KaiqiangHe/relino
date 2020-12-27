package com.relino.core.task;

import com.relino.core.db.Store;
import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.model.JobStatus;
import com.relino.core.model.db.ExecuteTimeEntity;
import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
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

    private Store store;
    private int deadJobMinute;

    /**
     * @param store not null
     * @param deadJobMinute >= {@link MIN_DEAD_JOB_MINUTES}
     */
    public DeadJobWatchDog(Store store, int deadJobMinute) {
        Utils.checkNoNull(store);
        Utils.check(deadJobMinute, v -> v < MIN_DEAD_JOB_MINUTES, "deadJobMinute应该>=" + deadJobMinute + ", current = " + deadJobMinute);

        this.store = store;
        this.deadJobMinute = deadJobMinute;
    }

    @Override
    protected void execute0() {
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
                try {
                    Thread.sleep(10000);
                } catch (Throwable t) {
                    HandleException.handleUnExpectedException(t);
                }
            }
        }
        log.info("DeadJobWatchDog运行结束");
    }

    private boolean doWatchDeadJob() throws SQLException {
        boolean sleep = true;
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(deadJobMinute);
        long start = System.currentTimeMillis();
        try {
            store.beginTx();
            long lastExecuteOrder = Long.parseLong(store.kvSelectForUpdate(DEAD_JOB_WATCH_DOG_KEY));
            ExecuteTimeEntity entity = store.selectDeadJobByTime(startTime);
            if(entity != null) {
                long executeOrder = entity.getExecuteOrder();
                List<Long> deadJobs = store.getDeadJobs(lastExecuteOrder, executeOrder);
                if(!Utils.isEmpty(deadJobs)) {
                    store.updateDeadJobs(deadJobs, LocalDateTime.now());
                }

                store.kvUpdateValue(DEAD_JOB_WATCH_DOG_KEY, Long.toString(entity.getExecuteOrder()));
                store.deleteExecuteTimeRecord(entity.getId());
                sleep = false;

                if(log.isDebugEnabled()) {
                    log.debug("ScanRunnableDelayJob, ids = [{}], time = {}",
                            deadJobs.stream().map(v -> v + "").collect(Collectors.joining(",")),
                            System.currentTimeMillis() - start);
                }
            }

            store.commitTx();
        } catch (Throwable t) {
            store.rollbackTx();
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
}
