package com.relino.core.exception;

import com.relino.core.model.BaseJob;

/**
 * // TODO: 2020/11/21
 *
 * @author kaiqiang.he
 */
public class JobDuplicateException extends JobCreateException {

    public JobDuplicateException(BaseJob job) {
        super(job);
    }

    public JobDuplicateException(String message, BaseJob job) {
        super(message, job);
    }

    public JobDuplicateException(String message, Throwable cause, BaseJob job) {
        super(message, cause, job);
    }
}
