package com.relino.core.model;

import com.relino.core.exception.RelinoException;
import com.relino.core.model.Job.Oper;
import com.relino.core.model.db.JobEntity;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.bean.BeanManager;
import com.relino.core.support.bean.BeanWrapper;

/**
 * 将JobEntity转换为Job对象
 *
 * @author kaiqiang.he
 */
public class JobConverter implements ObjectConverter<JobEntity, Job> {

    private BeanManager<Action> actionBeanManager;
    private BeanManager<IRetryPolicy> retryPolicyBeanManager;

    public JobConverter(BeanManager<Action> actionBeanManager, BeanManager<IRetryPolicy> retryPolicyBeanManager) {
        this.actionBeanManager = actionBeanManager;
        this.retryPolicyBeanManager = retryPolicyBeanManager;
    }

    @Override
    public Job convert(JobEntity entity) {

        if(entity == null) {
            return null;
        }

        String actionId = entity.getMActionId();
        BeanWrapper<Action> action = actionBeanManager.getBeanWrapper(actionId);
        if(action == null) {
            throw new RelinoException("actionId=" + actionId + "不存在Action");
        }

        String retryPolicyId = entity.getMRetryPolicyId();
        BeanWrapper<IRetryPolicy> retryPolicy = retryPolicyBeanManager.getBeanWrapper(retryPolicyId);
        if(retryPolicy == null) {
            throw new RuntimeException("RetryPolicy不存在, retryPolicyId = " + retryPolicyId);
        }

        Oper mOper = new Oper(
                action,
                OperStatus.toEnum(entity.getMOperStatus()),
                entity.getMExecuteCount(),
                entity.getMMaxRetry(),
                retryPolicy
        );

        return new Job(
                entity.getId(),
                entity.getJobId(),
                entity.getIdempotentId(),
                entity.getJobCode(),
                entity.getIsDelayJob() == Utils.TRUE,
                entity.getBeginTime(),
                JobStatus.toEnum(entity.getJobStatus()),
                entity.getExecuteOrder(),
                entity.getWillExecuteTime(),

                JobAttr.asObj(entity.getCommonAttr()),
                mOper
        );

    }
}
