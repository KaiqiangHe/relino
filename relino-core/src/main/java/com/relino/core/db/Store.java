package com.relino.core.db;

import com.relino.core.model.Job;
import com.relino.core.model.db.ExecuteTimeEntity;
import com.relino.core.model.db.JobEntity;
import com.relino.core.model.JobStatus;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 *
 * @author kaiqiang.he
 */
public abstract class Store {

    protected DataSource dataSource;

    public Store(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // -----------------------------------------------------------
    // tx
    abstract public void beginTx() throws SQLException;

    abstract public void commitTx() throws SQLException;

    abstract public void rollbackTx() throws SQLException;

    public <T, R> R executeWithTx(TxFunction<T, R> func, T t) throws Exception {
        beginTx();
        try {
            R ret = func.execute(t);
            commitTx();
            return ret;
        } catch (Exception e) {
            rollbackTx();
            throw e;
        }
    }

    // -----------------------------------------------------------------------------
    /**
     * 插入一条新job，返回受影响的行数
     */
    abstract public int insertJob(Job job) throws SQLException;

    /**
     * 根据jobId查询
     *
     * @return nullable
     */
    abstract public JobEntity queryByJobId(String jobId) throws SQLException;

    /**
     * 更新job, 返回受影响的行数
     *
     * @param updateCommonAttr 是否更新commonAttr
     */
    abstract public int updateJob(Job job, boolean updateCommonAttr) throws SQLException;

    /**
     * select for update
     */
    abstract public String kvSelectForUpdate(String key) throws SQLException;

    abstract public int kvUpdateValue(String key, String newValue) throws SQLException;

    /**
     * @return not null
     */
    abstract public List<JobEntity> selectJobEntity(long lastExecuteJobId, int limit) throws SQLException;

    /**
     * 返回willExecuteTime在[start, end] 且 jobStatus为{@link JobStatus#DELAY} 的limit个job
     *
     * @return not null, 如果无数据返回空list
     */
    abstract public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) throws SQLException;

    /**
     * 设置delay job 的executeOrder, 和状态为{@link JobStatus#RUNNABLE}
     *
     * @param updateData executeOrder
     */
    abstract public void setDelayJobRunnable(List<IdAndExecuteOrder> elems) throws SQLException;

    abstract public void insertExecuteRecord(long executeOrder, LocalDateTime time) throws SQLException;

    abstract public ExecuteTimeEntity selectDeadJobByTime(LocalDateTime time) throws SQLException;

    abstract public int deleteExecuteTimeRecord(long id) throws SQLException;

    /**
     * 查询处于(startExecuteOrder, endExecuteOrder] 且 jobStatus = 2
     *
     * @return not null
     */
    abstract public List<Long> getDeadJobs(long startExecuteOrder, long endExecuteOrder) throws SQLException;

    /**
     * 设置job为延迟job
     */
    abstract public int updateDeadJobs(List<Long> ids, LocalDateTime willExecuteTime) throws SQLException;
}