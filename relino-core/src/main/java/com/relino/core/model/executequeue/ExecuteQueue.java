package com.relino.core.model.executequeue;

import com.relino.core.model.BaseJob;

import java.util.List;

/**
 * 可执行job的队列
 *
 * @author kaiqiang.he
 */
public interface ExecuteQueue {

    /**
     * 获取下一批可执行的Job
     *
     * @param batchSize > 0
     * @return not null, may be empty
     */
    List<BaseJob> getNextExecutableJob(int batchSize) throws Exception;
}
