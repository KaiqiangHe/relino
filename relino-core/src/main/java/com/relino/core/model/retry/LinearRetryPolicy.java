package com.relino.core.model.retry;

import com.relino.core.support.Utils;

/**
 * 线性增长重试策略
 *
 * 重试时间 = k * executeCount
 *
 * @author kaiqiang.he
 */
public class LinearRetryPolicy implements IRetryPolicy {

    private int k;

    public LinearRetryPolicy(int k) {
        Utils.check(k, v -> v <= 0, "参数k[" + k + "]应大于0");
        this.k = k;
    }

    @Override
    public int retryAfterSeconds(int executeCount) {
        return k * executeCount;
    }

    public int getK() {
        return k;
    }
}
