package com.relino.core.exception;

import com.relino.core.model.BaseJob;

/**
 * job 创建失败异常
 */
public class JobCreateException extends RuntimeException {

    private BaseJob job;

    public JobCreateException(BaseJob job) {
        this.job = job;
    }

    public JobCreateException(String message, BaseJob job) {
        super(message);
        this.job = job;
    }

    public JobCreateException(String message, Throwable cause, BaseJob job) {
        super(message, cause);
        this.job = job;
    }

    public BaseJob getJob() {
        return job;
    }
}
