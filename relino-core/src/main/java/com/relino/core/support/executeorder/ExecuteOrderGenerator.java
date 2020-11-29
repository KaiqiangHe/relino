package com.relino.core.support.executeorder;

import java.util.List;

/**
 * // TODO: 2020/11/29  可移出该类, 通过mysql悲观锁实现
 * 获取job的执行顺序, 应保证在分布式环境下, 多台机器的order单调递增
 *
 * @author kaiqiang.he
 */
public interface ExecuteOrderGenerator {

    /**
     * 获取execute order
     */
    long getNextExecuteOrder();

    /**
     * 获取一批execute order
     *
     * @param batchSize > 0
     * @return not null
     */
    List<Long> getBatchNextExecuteOrder(int batchSize);
}
