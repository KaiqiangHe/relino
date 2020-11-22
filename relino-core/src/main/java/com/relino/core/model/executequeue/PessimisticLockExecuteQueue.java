package com.relino.core.model.executequeue;

import com.relino.core.db.Store;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.JobEntity;
import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
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

    private static final Logger log = LoggerFactory.getLogger(PessimisticLockExecuteQueue.class);

    private Store store;

    public PessimisticLockExecuteQueue(Store store) {
        this.store = store;
    }

    /**
     * 注意：
     * 一次只允许一个线程执行, 防止多个线程并发而大量占用数据库链接
     */
    @Override
    public synchronized List<Job> getNextExecutableJob(int batchSize) throws Exception {

        if(batchSize < 1) {
            throw new IllegalArgumentException("参数batchSize比速大于0, 当前值为" + batchSize);
        }

        DataSource ds = store.getDataSource();
        Connection conn = ds.getConnection();

        try {
            conn.setAutoCommit(false);

            // acquire pessimistic lock
            // lock 直到事务提交
            String lastExecuteJobIdStr = store.kvSelectForUpdate(conn, EXECUTE_QUEUE_CURSOR);
            long lastExecuteJobId = Long.parseLong(lastExecuteJobIdStr);

            List<JobEntity> rows = store.selectJobEntity(lastExecuteJobId, batchSize);

            if(!Utils.isEmpty(rows)) {
                long lastExecuteOrder = rows.get(rows.size() - 1).getExecuteOrder();
                store.kvUpdateValue(conn, EXECUTE_QUEUE_CURSOR, Long.toString(lastExecuteOrder));
            }

            // release lock
            conn.commit();

            if(Utils.isEmpty(rows)) {
                return Collections.emptyList();
            } else {
                // TODO: 2020/11/22
                return rows.stream().map(e -> {
                    return new Job(e.getId(), e.getJobId(), e.getJobId(), "", false, LocalDateTime.now(), new JobAttr(), null);
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("getExecutableJobId error ", e);
            conn.rollback(); // release lock
            throw e;
        } finally {
            if(conn != null) {
                conn.close();
            }
        }
    }
}
