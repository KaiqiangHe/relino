package com.relino.core;

import com.relino.core.exception.RelinoException;
import com.relino.core.model.Action;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.bean.BeanManager;
import com.relino.core.support.bean.BeanWrapper;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobBuilder {

    private final BeanManager<IRetryPolicy> retryPolicyBeanManager;
    private final BeanManager<Action> actionBeanManager;

    private final String jobId;
    private String idempotentId;
    private final BeanWrapper<Action> action;

    private String jobCode = Job.DEFAULT_JOB_CODE;
    private boolean delayJob = false;
    private LocalDateTime beginTime = LocalDateTime.now();
    private JobAttr commonAttr = new JobAttr();

    private int maxExecuteCount = Job.Oper.DEFAULT_MAX_RETRY_COUNT;
    private BeanWrapper<IRetryPolicy> retryPolicy;

    public JobBuilder(String jobId, String actionId, BeanManager<Action> actionBeanManager, BeanManager<IRetryPolicy> retryPolicyBeanManager) {
        Utils.checkNoNull(actionBeanManager);
        Utils.checkNoNull(retryPolicyBeanManager);
        Utils.checkNonEmpty(jobId);
        Utils.checkNonEmpty(actionId);

        this.retryPolicyBeanManager = retryPolicyBeanManager;
        this.actionBeanManager = actionBeanManager;

        this.jobId = jobId;
        this.idempotentId = jobId;
        this.retryPolicy = retryPolicyBeanManager.getBeanWrapper(Relino.DEFAULT_RETRY_POLICY);

        BeanWrapper<Action> action = actionBeanManager.getBeanWrapper(actionId);
        if(action == null) {
            throw new RelinoException("actionId=" + actionId + "不存在Action");
        }
        this.action = action;
    }

    public JobBuilder idempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
        return this;
    }

    //
    /*public JobBuilder jobCode(String jobCode) {
        this.jobCode = jobCode;
        return this;
    }*/

    public JobBuilder delayExecute(int delaySeconds) {
        this.delayJob = true;
        this.beginTime = LocalDateTime.now().plusSeconds(delaySeconds);
        return this;
    }

    public JobBuilder delayExecute(LocalDateTime executeTime) {
        this.delayJob = true;
        this.beginTime = executeTime;
        return this;
    }

    public JobBuilder commonAttr(JobAttr commonAttr) {
        this.commonAttr = commonAttr;
        return this;
    }

    public JobBuilder maxExecuteCount(int maxRetry) {
        this.maxExecuteCount = maxRetry;
        return this;
    }

    public JobBuilder retryPolicy(String retryPolicyId) {
        BeanWrapper<IRetryPolicy> retryPolicy = retryPolicyBeanManager.getBeanWrapper(retryPolicyId);
        if(retryPolicy == null) {
            throw new RuntimeException("RetryPolicy不存在, retryPolicyId = " + retryPolicyId);
        }
        this.retryPolicy = retryPolicy;
        return this;
    }

    public Job build() {
        return new Job(this);
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

    public BeanWrapper<Action> getAction() {
        return action;
    }

    public int getMaxExecuteCount() {
        return maxExecuteCount;
    }

    public BeanWrapper<IRetryPolicy> getRetryPolicy() {
        return retryPolicy;
    }

}
