package com.relino.core.db;

import com.relino.core.model.Job;
import com.relino.core.model.JobStatus;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public interface Store {

    /**
     * 插入一条新job，返回受影响的行数
     */
    int insertJob(Job job) throws SQLException;

    /**
     * 更新job, 返回受影响的行数
     *
     * @param updateCommonAttr 是否更新commonAttr
     */
    int updateJob(Job job, boolean updateCommonAttr) throws SQLException;

    /**
     * 返回willExecuteTime在[start, end] 且 jobStatus为{@link JobStatus#DELAY} 的limit个job
     *
     * @return not null, 如果无数据返回空list
     */
    List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit);

    /**
     * 设置delay job 的executeOrder, 和状态为{@link JobStatus#RUNNABLE}
     *
     * @param updateData executeOrder
     */
    void setDelayJobRunnable(List<IdAndExecuteOrder> updateData);



}
