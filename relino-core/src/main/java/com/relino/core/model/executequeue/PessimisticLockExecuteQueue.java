package com.relino.core.model.executequeue;

import com.relino.core.model.Job;
import com.relino.core.model.JobStatus;
import com.relino.core.model.db.JobEntity;
import com.relino.core.support.JobUtils;
import com.relino.core.support.Utils;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.DBPessimisticLock;
import com.relino.core.support.db.KVStore;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于mysql悲观锁
 *
 * @author kaiqiang.he
 */
public class PessimisticLockExecuteQueue implements RunnableExecuteQueue {

    private static final String EXECUTE_QUEUE_CURSOR = "execute_queue_cursor";

    private DBExecutor dbExecutor;
    private KVStore kvStore;
    private DBPessimisticLock dbPessimisticLock;

    public PessimisticLockExecuteQueue(DBExecutor dbExecutor) {
        Utils.checkNoNull(dbExecutor);
        this.dbExecutor = dbExecutor;
        kvStore = new KVStore(dbExecutor);
        dbPessimisticLock = new DBPessimisticLock(dbExecutor);
    }

    /**
     * 注意：
     * 一次只允许一个线程执行, 防止多个线程并发而大量占用数据库链接
     *
     * @return not null, maybe empty
     */
    @Override
    public synchronized List<Job> getNextRunnableJob(int batchSize) throws Exception {

        if(batchSize < 1) {
            throw new IllegalArgumentException("参数batchSize比速大于0, 当前值为" + batchSize);
        }

        return dbExecutor.executeWithTx(
                t -> {
                    dbPessimisticLock.openPessimisticLock(EXECUTE_QUEUE_CURSOR);
                    long lastExecuteJobId = Long.parseLong(kvStore.get(EXECUTE_QUEUE_CURSOR));

                    List<JobEntity> rows = getNextRunnableJob(lastExecuteJobId, batchSize);

                    if(!Utils.isEmpty(rows)) {
                        long lastExecuteOrder = rows.get(rows.size() - 1).getExecuteOrder();
                        logExecuteTime(lastExecuteOrder, LocalDateTime.now());
                        kvStore.update(EXECUTE_QUEUE_CURSOR, Long.toString(lastExecuteOrder));
                    }

                    if(Utils.isEmpty(rows)) {
                        return Collections.emptyList();
                    } else {
                        return rows.stream().map(JobEntity::toJob).collect(Collectors.toList());
                    }
                }, null
        );
    }

    public static final String GET_NEXT_RUNNABLE_JOB =
            "select * from job where job_status = " + JobStatus.RUNNABLE.getCode() + " and execute_order > ? order by execute_order limit ?";
    /**
     * 获取下一批可执行的Job
     *
     * @return not null, maybe empty
     */
    public List<JobEntity> getNextRunnableJob(long lastExecuteJobId, int limit) throws SQLException {
        List<Map<String, Object>> rows = dbExecutor.query(GET_NEXT_RUNNABLE_JOB, new MapListHandler(), new Object[]{lastExecuteJobId, limit});
        if(Utils.isEmpty(rows)) {
            return Collections.emptyList();
        }

        return rows.stream().map(JobUtils::toJobEntity).collect(Collectors.toList());
    }

    /**
     * 插入执行时间日志
     */
    public void logExecuteTime(long executeOrder, LocalDateTime time) throws SQLException {
        String sql = "insert into execute_time(execute_order, execute_job_time) value (?, ?)";
        Object[] params = new Object[]{executeOrder, Utils.toStrDate(time)};

        dbExecutor.execute(sql, params);
    }
}
