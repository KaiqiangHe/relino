package com.relino.core.model;

import com.relino.core.model.retry.IRetryPolicyManager;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobBuilderV2 {

    private final String jobId;
    private final String actionId;

    private String idempotentId;

    private int maxExecuteCount = Oper.DEFAULT_MAX_RETRY_COUNT;
    private String retryPolicyId = IRetryPolicyManager.DEFAULT_RETRY_POLICY;

    private String jobCode = BaseJob.DEFAULT_JOB_CODE;
    private boolean delayJob = false;
    private LocalDateTime beginTime = LocalDateTime.now();
    private JobAttr commonAttr = new JobAttr();

    public JobBuilderV2(String jobId, String actionId) {
        this.jobId = jobId;
        this.actionId = actionId;
        this.idempotentId = jobId;
    }

    public JobBuilderV2 idempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
        return this;
    }

    public JobBuilderV2 jobCode(String jobCode) {
        this.jobCode = jobCode;
        return this;
    }

    public JobBuilderV2 delayJob(int delaySeconds) {
        this.delayJob = true;
        this.beginTime = LocalDateTime.now().plusSeconds(delaySeconds);
        return this;
    }

    public JobBuilderV2 delayJob(LocalDateTime delayTime) {
        this.delayJob = true;
        this.beginTime = delayTime;
        return this;
    }

    public JobBuilderV2 commonAttr(JobAttr commonAttr) {
        this.commonAttr = commonAttr;
        return this;
    }

    public String getJobId() {
        return jobId;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public String getJobCode() {
        return jobCode;
    }

    public boolean isDelayJob() {
        return delayJob;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public JobAttr getCommonAttr() {
        return commonAttr;
    }

    public String getActionId() {
        return actionId;
    }
}
