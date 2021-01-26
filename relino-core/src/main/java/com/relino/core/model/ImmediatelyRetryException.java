package com.relino.core.model;

/**
 * 立即重试异常
 *
 * @author kaiqiang.he
 */
public class ImmediatelyRetryException extends RuntimeException {

    public ImmediatelyRetryException(String message) {
        super(message);
    }

    public ImmediatelyRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
