package com.relino.core.task;

import com.relino.core.JobBuilder;
import com.relino.core.exception.JobCreateException;
import com.relino.core.exception.JobDuplicateException;
import com.relino.core.model.Job;

/**
 * @author kaiqiang.he
 */
public interface JobFactory {

    /**
     * 创建一个job
     *
     * @throws JobCreateException
     * @throws JobDuplicateException
     */
    void createJob(Job job) throws JobCreateException, JobDuplicateException;

    /**
     * 创建一个JobBuilder
     *
     * @param actionId not empty
     */
    JobBuilder builder(String actionId);
}
