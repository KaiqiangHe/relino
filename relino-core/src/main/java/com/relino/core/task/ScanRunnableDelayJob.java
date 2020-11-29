package com.relino.core.task;

import com.relino.core.db.IdAndExecuteOrder;
import com.relino.core.db.Store;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.JacksonSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public final class ScanRunnableDelayJob extends AbstractRunSupport {

    private static final Logger log = LoggerFactory.getLogger(ScanRunnableDelayJob.class);

    private static final String EXECUTE_ORDER_KEY = "execute_order";
    private static final int BATCH_SIZE = 200;
    public static final LocalDateTime START_DATE_TIME =
            LocalDateTime.of(1970, 1, 1, 0, 0, 1);

    private Store store;
    private int batchSize;

    public ScanRunnableDelayJob(Store store, int batchSize) {
        this.store = store;
        this.batchSize = batchSize;
    }

    @Override
    protected void execute0() throws SQLException {

        while (true) {

            if(wannaStop()) {
                break;
            }

            boolean sleep = true;
            LocalDateTime now = LocalDateTime.now();
            long start = System.currentTimeMillis();
            try {
                store.beginTx();
                String value = store.kvSelectForUpdate(EXECUTE_ORDER_KEY);
                long executeOrder = Long.parseLong(value);

                // 获取一批可执行的延迟job
                // TODO: 2020/11/28  START_DATE_TIME 这可以优化
                List<Long> ids = store.getRunnableDelayJobId(START_DATE_TIME, now, batchSize);
                if(ids.isEmpty()) {
                    sleep = true;
                    log.debug("无延迟job, time = {}", System.currentTimeMillis() - start);
                } else {
                    // 更新可执行job
                    List<IdAndExecuteOrder> updateData = new ArrayList<>();
                    for (Long id : ids) {
                        executeOrder++;
                        updateData.add(new IdAndExecuteOrder(id, executeOrder));
                    }
                    store.setDelayJobRunnable(updateData);

                    // 更新executeOrder
                    store.kvUpdateValue(EXECUTE_ORDER_KEY, Long.toString(executeOrder));

                    sleep = false;
                    log.debug("更新延迟job, ids = {}, time = {}", JacksonSupport.toJson(ids), System.currentTimeMillis() - start);
                }

                store.commitTx();
            } catch (Exception e) {
                store.rollbackTx();
                log.error("error ", e);
            }

            if(wannaStop()) {
                break;
            }

            if(sleep) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("error ", e);
                }
            }
        }

    }
}