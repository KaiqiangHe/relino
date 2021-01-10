package com.relino.core.model.executequeue;

import com.relino.core.db.Store;
import com.relino.core.model.BaseJob;
import com.relino.core.model.db.JobEntity;
import com.relino.core.support.Utils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于mysql悲观锁
 *
 * @author kaiqiang.he
 */
public class PessimisticLockExecuteQueue implements ExecuteQueue {

    private static final String EXECUTE_QUEUE_CURSOR = "execute_queue_cursor";

    private Store store;

    public PessimisticLockExecuteQueue(Store store) {
        this.store = store;
    }

    /**
     * 注意：
     * 一次只允许一个线程执行, 防止多个线程并发而大量占用数据库链接
     *
     * @return not null, maybe empty
     */
    @Override
    public synchronized List<BaseJob> getNextExecutableJob(int batchSize) throws Exception {

        if(batchSize < 1) {
            throw new IllegalArgumentException("参数batchSize比速大于0, 当前值为" + batchSize);
        }

        return store.executeWithTx(
                t -> {
                    String lastExecuteJobIdStr = store.kvSelectForUpdate(EXECUTE_QUEUE_CURSOR);
                    long lastExecuteJobId = Long.parseLong(lastExecuteJobIdStr);

                    List<JobEntity> rows = store.selectJobEntity(lastExecuteJobId, batchSize);

                    if(!Utils.isEmpty(rows)) {
                        long lastExecuteOrder = rows.get(rows.size() - 1).getExecuteOrder();
                        store.insertExecuteRecord(lastExecuteOrder, LocalDateTime.now());
                        store.kvUpdateValue(EXECUTE_QUEUE_CURSOR, Long.toString(lastExecuteOrder));
                    }

                    if(Utils.isEmpty(rows)) {
                        return Collections.emptyList();
                    } else {
                        return rows.stream().map(JobEntity::toJob).collect(Collectors.toList());
                    }
                }, null
        );
    }
}
