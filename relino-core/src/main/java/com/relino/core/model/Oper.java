package com.relino.core.model;

/**
 * @author kaiqiang.he
 */
public class Oper {

    /**
     * 默认最大重试次数
     */
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    /**
     * actionId
     */
    private final String actionId;

    /**
     * 当前oper的状态
     */
    private OperStatus operStatus;

    /**
     * 已执行次数
     */
    private int executeCount = 0;

    /**
     * action 执行的结果
     */
    private ActionResult executeResult;

    // ---------------------------------------------------
    // 重试
    /**
     * 最大重试次数
     */
    private final int maxExecuteCount;

    /**
     * 重试策略
     */
    private final String retryPolicyId;

    public Oper(String actionId, OperStatus operStatus, int executeCount,
                int maxExecuteCount, String retryPolicyId) {
        this.actionId = actionId;
        this.operStatus = operStatus;
        this.executeCount = executeCount;
        this.maxExecuteCount = maxExecuteCount;
        this.retryPolicyId = retryPolicyId;
    }

}
