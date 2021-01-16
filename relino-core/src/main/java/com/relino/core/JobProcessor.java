package com.relino.core;

import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.Oper;
import com.relino.core.support.Utils;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.thread.Processor;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobProcessor implements Processor<Job> {

    private JobStore jobStore;

    public JobProcessor(JobStore jobStore) {
        Utils.checkNoNull(jobStore);
        this.jobStore = jobStore;
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

            boolean retryNow = false;
            if(mOper.isExecuteFinished()) {
                job.finished();
            } else {
                // 执行未完成、重试
                LocalDateTime retryExecuteTime = mOper.getRetryExecuteTime();
                if(retryExecuteTime == null) {
                    retryNow = true;    // 立即重试
                } else {
                    job.delayExecute(retryExecuteTime);
                }
            }

            jobStore.updateJob(job, updateCommonAttr);

            if(retryNow) {
                process(job);
            }

        } catch (Exception e) {
            HandleException.handleUnExpectedException(e);
        }
    }
}
