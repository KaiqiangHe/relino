package com.relino.core.model.retry;

import com.relino.core.support.Utils;

/**
 * 线性增长重试策略
 *
 * 重试时间 = k * executeCount + b
 *
 * @author kaiqiang.he
 */
public class LinearRetryPolicy implements IRetryPolicy {

    private int k;

    private int b;

    public LinearRetryPolicy(int k) {
        this(k, 0);
    }

    public LinearRetryPolicy(int k, int b) {
        Utils.check(k, v -> v <= 0, "参数k[" + k + "]应大于0");
        Utils.check(b, v -> v < 0, "参数b[" + k + "]应大于等于0");
        this.k = k;
        this.b = b;
    }

    @Override
    public int retryAfterSeconds(int executeCount) {
        return k * executeCount;
    }

    public int getK() {
        return k;
    }

    public int getB() {
        return b;
    }
}
