package com.relino.core;

import com.relino.core.db.Store;
import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.Oper;
import com.relino.core.support.Utils;
import com.relino.core.support.thread.Processor;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobProcessor implements Processor<Job> {

    private Store store;

    public JobProcessor(Store store) {
        this.store = store;
    }

    @Override
    public void process(Job job) {

        Utils.checkNoNull(job);

        try {
            String jobId = job.getJobId();
            Oper mOper = job.getMOper();
            JobAttr commonAttr = job.getCommonAttr();

            mOper.execute(jobId, commonAttr);
            boolean updateCommonAttr = false;
            JobAttr resultValue = mOper.getExecuteResult().getResultValue();
            if(!resultValue.isEmpty()) {
                updateCommonAttr = true;
                commonAttr.addAll(resultValue);
            }

            if(mOper.isExecuteFinished()) {
                job.setExecuteFinish();
            } else {
                // 执行未完成、重试
                LocalDateTime retryExecuteTime = mOper.getRetryExecuteTime();
                if(retryExecuteTime == null) {
                    job.setImmediatelyRetryExecute();
                } else {
                    job.setDelayExecute(retryExecuteTime);
                }
            }

            store.updateJob(job, updateCommonAttr);

            if(job.isRetryNow()) {
                process(job);
            }

        } catch (Exception e) {
            HandleException.handleUnExpectedException(e);
        }
    }
}
