package com.relino.core;

import com.relino.core.exception.JobCreateException;
import com.relino.core.exception.JobDuplicateException;
import com.relino.core.exception.RelinoException;
import com.relino.core.model.Action;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.bean.BeanManager;
import com.relino.core.support.bean.BeanWrapper;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.id.IdGenerator;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobFactory {

    private JobStore jobStore;
    private IdGenerator idGenerator;
    private BeanManager<Action> actionBeanManager;
    private BeanManager<IRetryPolicy> retryPolicyBeanManager;

    public JobFactory(JobStore jobStore, IdGenerator idGenerator,
                      BeanManager<Action> actionBeanManager, BeanManager<IRetryPolicy> retryPolicyBeanManager) {
        this.retryPolicyBeanManager = retryPolicyBeanManager;

        Utils.checkNoNull(jobStore);
        Utils.checkNoNull(idGenerator);
        Utils.checkNoNull(actionBeanManager);

        this.jobStore = jobStore;
        this.idGenerator = idGenerator;
        this.actionBeanManager = actionBeanManager;
    }

    /**
     * 创建一个job
     *
     * @throws JobCreateException
     * @throws JobDuplicateException
     */
    public void createJob(Job job) throws JobCreateException, JobDuplicateException {
        try {
            jobStore.insertNew(job);
        } catch (Exception e) {
            throw new JobCreateException("创建job失败", e, job);
        }
    }

    public JobBuilder builder(String actionId) {

        BeanWrapper<Action> action = actionBeanManager.getBeanWrapper(actionId);

        if(action == null) {
            throw new RelinoException("actionId=" + actionId + "不存在Action");
        }

        String jobId = idGenerator.getNext();
        return new JobBuilder(jobId, action, retryPolicyBeanManager);
    }

    public static class JobBuilder {

        private final BeanManager<IRetryPolicy> retryPolicyBeanManager;

        private final String jobId;
        private String idempotentId;
        private final BeanWrapper<Action> action;

        private String jobCode = Job.DEFAULT_JOB_CODE;
        private boolean delayJob = false;
        private LocalDateTime beginTime = LocalDateTime.now();
        private JobAttr commonAttr = new JobAttr();

        private int maxExecuteCount = Job.Oper.DEFAULT_MAX_RETRY_COUNT;
        private BeanWrapper<IRetryPolicy> retryPolicy;

        public JobBuilder(String jobId, BeanWrapper<Action> action, BeanManager<IRetryPolicy> retryPolicyBeanManager) {
            this.retryPolicyBeanManager = retryPolicyBeanManager;
            this.jobId = jobId;
            this.idempotentId = jobId;
            this.action = action;
            this.retryPolicy = retryPolicyBeanManager.getBeanWrapper(Relino.DEFAULT_RETRY_POLICY);
        }

        public JobBuilder idempotentId(String idempotentId) {
            this.idempotentId = idempotentId;
            return this;
        }

        public JobBuilder jobCode(String jobCode) {
            this.jobCode = jobCode;
            return this;
        }

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
}
