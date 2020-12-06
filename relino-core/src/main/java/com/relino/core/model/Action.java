package com.relino.core.model;

/**
 * @author kaiqiang.he
 */
public interface Action {

    /**
     * @param commonAttr 共享属性
     * @param executeCount 当前job第几次执行, 第一次执行时值为1
     */
    ActionResult execute(String jobId, JobAttr commonAttr, int executeCount);

}
