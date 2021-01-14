package com.relino.core.model;

import com.relino.core.exception.RelinoException;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.model.retry.IRetryPolicyManager;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class Oper {

    /**
     * 默认最大重试次数
     */
    public static final int DEFAULT_MAX_RETRY_COUNT = 3;

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

    /**
     * 执行当前oper
     */
    public void execute(String jobId, JobAttr commonAttr) {
        Action action = ActionManager.getAction(actionId);
        if(action == null) {
            throw new RelinoException("Action不存在, actionId = " + actionId);
        }

        executeCount ++;
        executeResult = action.execute(jobId, commonAttr, executeCount);

        if(executeResult.isSuccess()) {
            operStatus = OperStatus.SUCCESS_FINISHED;
        } else {
            if(executeCount >= maxExecuteCount) {
                operStatus = OperStatus.FAILED_FINISHED;
            }
        }
    }

    /**
     * 获取延迟执行的时间, 如果为null 立即重试
     */
    public LocalDateTime getRetryExecuteTime() {
        IRetryPolicy retryPolicy = IRetryPolicyManager.getIRetryAfter(retryPolicyId);
        if(retryPolicy == null) {
            throw new RuntimeException("RetryPolicy不存在, retryPolicyId = " + retryPolicyId);
        }
        int delaySeconds = retryPolicy.retryAfterSeconds(executeCount);
        if(delaySeconds == 0) {
            return null;
        } else {
            return LocalDateTime.now().plusSeconds(delaySeconds);
        }
    }

    /**
     * 执行是否结束
     */
    public boolean isExecuteFinished() {
        return operStatus == OperStatus.SUCCESS_FINISHED || operStatus == OperStatus.FAILED_FINISHED;
    }

    // -----------------------------------------------------------------------------------

    public void setOperStatus(OperStatus operStatus) {
        this.operStatus = operStatus;
    }

    public void setExecuteCount(int executeCount) {
        this.executeCount = executeCount;
    }

    public void setExecuteResult(ActionResult executeResult) {
        this.executeResult = executeResult;
    }

    public String getActionId() {
        return actionId;
    }

    public OperStatus getOperStatus() {
        return operStatus;
    }

    public int getExecuteCount() {
        return executeCount;
    }

    public ActionResult getExecuteResult() {
        return executeResult;
    }

    public int getMaxExecuteCount() {
        return maxExecuteCount;
    }

    public String getRetryPolicyId() {
        return retryPolicyId;
    }

    // -----------------------------------------------------------------------------------------
    // builder start
    private Oper(OperBuilder builder) {
        this.actionId = builder.actionId;
        this.maxExecuteCount = builder.maxExecuteCount;
        this.retryPolicyId = builder.retryPolicyId;
        this.operStatus = OperStatus.RUNNABLE;
        this.executeCount = 0;
        this.executeResult = null;
    }

    public static OperBuilder builder(String actionId) {
        return new OperBuilder(actionId);
    }
    public static class OperBuilder {
        private final String actionId;
        private int maxExecuteCount = DEFAULT_MAX_RETRY_COUNT;
        private String retryPolicyId = IRetryPolicyManager.DEFAULT_RETRY_POLICY;

        public OperBuilder(String actionId) {
            if(!ActionManager.containsAction(actionId)) {
                throw new RelinoException("actionId=" + actionId + "不存在");
            }
            this.actionId = actionId;
        }

        public OperBuilder maxExecuteCount(int maxRetry) {
            if(maxRetry < 1) {
                throw new IllegalArgumentException("Parameter 'maxRetry' should >=1, actual = " + maxRetry);
            }
            this.maxExecuteCount = maxRetry;
            return this;
        }

        public OperBuilder retryPolicy(String retryPolicyId) {
            this.retryPolicyId = retryPolicyId;
            return this;
        }

        public Oper build() {
            return new Oper(this);
        }
    }
    // builder end
    // ----------------------------------------------------------------------

}
