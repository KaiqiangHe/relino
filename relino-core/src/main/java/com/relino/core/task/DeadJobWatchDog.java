package com.relino.core.task;

import com.relino.core.db.Store;
import com.relino.core.model.db.ExecuteTimeEntity;
import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 扫描执行后超过指定时间未更新的Job
 *
 * @author kaiqiang.he
 */
public class DeadJobWatchDog extends AbstractRunSupport implements ElectionCandidate {

    private static final Logger log = LoggerFactory.getLogger(DeadJobWatchDog.class);

    private static final String DEAD_JOB_WATCH_DOG_KEY = "dead_job_watch_dog";

    private Store store;
    private int watchMinute;

    public DeadJobWatchDog(Store store, int watchMinute) {
        this.store = store;
        this.watchMinute = watchMinute;
    }

    @Override
    protected void execute0() throws SQLException {
        while (true) {

            if (wannaStop()) {
                break;
            }

            boolean sleep = true;
            LocalDateTime startTime = LocalDateTime.now().minusMinutes(watchMinute);
            long start = System.currentTimeMillis();
            try {
                store.beginTx();
                long lastExecuteOrder = Long.parseLong(store.kvSelectForUpdate(DEAD_JOB_WATCH_DOG_KEY));
                ExecuteTimeEntity entity = store.selectDeadJobByTime(startTime);
                if(entity != null) {
                    long executeOrder = entity.getExecuteOrder();
                    List<Long> deadJobs = store.getDeadJobs(lastExecuteOrder, executeOrder);
                    if(!Utils.isEmpty(deadJobs)) {
                        log.info("watchdog dead job, ids = {}", deadJobs);
                        store.updateDeadJobs(deadJobs, LocalDateTime.now());
                    }

                    store.kvUpdateValue(DEAD_JOB_WATCH_DOG_KEY, Long.toString(entity.getExecuteOrder()));
                    store.deleteExecuteTimeRecord(entity.getId());
                    sleep = false;
                }

                log.info("watchdog execute, time = {}", System.currentTimeMillis() - start);
                store.commitTx();
            } catch (Exception e) {
                store.rollbackTx();
                log.error("execute error ", e);
                sleep = true;
            }

            if (wannaStop()) {
                break;
            }

            if(sleep) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error("error ", e);
                }
            }
        }
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
