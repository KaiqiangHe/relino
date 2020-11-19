package com.relino.core.model.retry;

/**
 * 默认重试策略
 *
 * 重试时间 = 5 * executeCount
 *
 * @author kaiqiang.he
 */
public class DefaultIRetryPolicy implements IRetryPolicy {

    @Override
    public int retryAfterSeconds(int executeCount) {
        return 5 * executeCount;
    }

}
