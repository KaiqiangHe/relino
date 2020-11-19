package com.relino.core.model.retry;

/**
 * 立即重试
 *
 * @author kaiqiang.he
 */
public class ImmediatelyRetryPolicy implements IRetryPolicy {

    @Override
    public int retryAfterSeconds(int executeCount) {
        return 0;
    }
}
