package com.relino.core.model;

/**
 * @author kaiqiang.he
 */
public interface Action {

    /**
     *
     *
     * @param commonAttr 共享属性
     * @param executeCount 当前job第几次执行, 第一次执行时值为1
     *
     * @throws ImmediatelyRetryException 如果需要立即重试，抛出该异常
     * @throws DelayRetryException 如果需要延迟重试，抛出该异常
     * @throws Exception 按照默认重试策略重试
     */
    ActionResult execute(String jobId, JobAttr commonAttr, int executeCount);

}
