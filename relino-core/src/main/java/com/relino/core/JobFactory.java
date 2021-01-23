package com.relino.core;

import com.relino.core.exception.JobCreateException;
import com.relino.core.exception.JobDuplicateException;
import com.relino.core.model.Action;
import com.relino.core.model.Job;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.bean.BeanManager;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.id.IdGenerator;

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
        String jobId = idGenerator.getNext();
        return new JobBuilder(jobId, actionId, actionBeanManager, retryPolicyBeanManager);
    }
}
