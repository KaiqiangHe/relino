package com.relino.core.exception;

import com.relino.core.model.Job;

/**
 * // TODO: 2020/11/21
 *
 * @author kaiqiang.he
 */
public class JobDuplicateException extends JobCreateException {

    public JobDuplicateException(Job job) {
        super(job);
    }

    public JobDuplicateException(String message, Job job) {
        super(message, job);
    }

    public JobDuplicateException(String message, Throwable cause, Job job) {
        super(message, cause, job);
    }
}
