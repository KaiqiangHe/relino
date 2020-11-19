package com.relino.core.db;

import com.relino.core.model.Job;
import com.relino.core.model.JobStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public interface Store {

    /**
     * 插入一条新job
     */
    void insertJob(Job job);

    /**
     * @param updateCommonAttr 是否更新commonAttr
     */
    void updateJob(Job job, boolean updateCommonAttr);

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
