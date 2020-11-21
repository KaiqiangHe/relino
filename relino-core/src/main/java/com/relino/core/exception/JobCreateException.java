package com.relino.core.exception;

import com.relino.core.model.Job;

/**
 * job 创建失败异常
 */
public class JobCreateException extends RuntimeException {

    private Job job;

    public JobCreateException(Job job) {
        this.job = job;
    }

    public JobCreateException(String message, Job job) {
        super(message);
        this.job = job;
    }

    public JobCreateException(String message, Throwable cause, Job job) {
        super(message, cause);
        this.job = job;
    }

    public Job getJob() {
        return job;
    }
}
