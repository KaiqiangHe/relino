package com.relino.core.support.executeorder;

import com.relino.core.support.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于时间递增, 应保证在分布式环境下同一时刻只有一个实例执行
 *
 * 仅仅为了测试-不能用于生产
 *
 * @author kaiqiang.he
 */
public class TimeExecuteOrderGenerator implements ExecuteOrderGenerator {

    private AtomicLong executeOrder = new AtomicLong(System.currentTimeMillis() * 1000000);

    /**
     * 获取延迟jod的执行顺序
     */
    @Override
    public synchronized long getNextExecuteOrder() {
        return executeOrder.incrementAndGet();
    }

    @Override
    public synchronized List<Long> getBatchNextExecuteOrder(int batchSize) {
        Utils.check(batchSize, v -> v < 1, "参数batchSize应大于0, [" + batchSize + "]");

        List<Long> ret = new ArrayList<>(batchSize);
        for (int i = 0; i < batchSize; i++) {
            ret.add(executeOrder.getAndIncrement());
        }

        return ret;
    }
}
