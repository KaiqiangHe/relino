package com.relino.core.model.retry;

/**
 *
 * @author kaiqiang.he
 */
public interface IRetryPolicy {

    /**
     * @param executeCount 当前已经执行的次数, 大于0
     * @return 延迟执行的秒数。小于等于0则立即重试
     */
    int retryAfterSeconds(int executeCount);
}
