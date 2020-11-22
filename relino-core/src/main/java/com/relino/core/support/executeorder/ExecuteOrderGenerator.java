package com.relino.core.support.executeorder;

import java.util.List;

/**
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
