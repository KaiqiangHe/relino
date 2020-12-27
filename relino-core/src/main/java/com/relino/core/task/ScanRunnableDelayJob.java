package com.relino.core.task;

import com.relino.core.db.IdAndExecuteOrder;
import com.relino.core.db.Store;
import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.JacksonSupport;
import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 扫描可以运行的job
 *
 * @author kaiqiang.he
 */
public final class ScanRunnableDelayJob extends AbstractRunSupport implements ElectionCandidate {

    private static final Logger log = LoggerFactory.getLogger(ScanRunnableDelayJob.class);

    public static final int MAX_BATCH_SIZE = 400;

    private static final String EXECUTE_ORDER_KEY = "execute_order";
    private static final int TIME_STEP = 10;

    private Store store;
    private int batchSize;

    /**
     * @param store not null
     * @param batchSize 属于[1, {@link MAX_BATCH_SIZE}]
     */
    public ScanRunnableDelayJob(Store store, int batchSize) {

        Utils.checkNoNull(store);
        Utils.check(batchSize, v -> v < 1 || v > MAX_BATCH_SIZE, "batchSize应该属于[1, " + MAX_BATCH_SIZE + "], current = " + batchSize);

        this.store = store;
        this.batchSize = batchSize;
    }

    @Override
    protected void execute0() {
        log.info("ScanRunnableDelayJob开始运行");
        while (true) {

            boolean sleep;
            try {
                if(wannaStop()) { break; }
                sleep = doScan();
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
                    Thread.sleep(500);
                } catch (Throwable t) {
                    HandleException.handleUnExpectedException(t);
                }
            }
        }
        log.info("ScanRunnableDelayJob运行结束");
    }

    /**
     * @return 返回主线程是否sleep
     */
    public boolean doScan() throws SQLException {

        boolean sleep;
        LocalDateTime now = LocalDateTime.now();
        long start = System.currentTimeMillis();
        try {
            store.beginTx();
            String value = store.kvSelectForUpdate(EXECUTE_ORDER_KEY);
            long executeOrder = Long.parseLong(value);

            // 获取一批可执行的延迟job
            List<Long> ids = store.getRunnableDelayJobId(now.minusMinutes(TIME_STEP), now, batchSize);
            if(ids.isEmpty()) {
                sleep = true;
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
            }

            if(log.isDebugEnabled()) {
                log.debug("ScanRunnableDelayJob, ids = [{}], time = {}",
                        ids.stream().map(v -> v + "").collect(Collectors.joining(",")),
                        System.currentTimeMillis() - start);
            }

            store.commitTx();
        } catch (Throwable t) {
            store.rollbackTx();
            sleep = true;
            HandleException.handleUnExpectedException(t);
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
