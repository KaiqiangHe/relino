package com.relino.core;

import com.relino.core.db.Store;
import com.relino.core.exception.JobCreateException;
import com.relino.core.exception.JobDuplicateException;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.Oper;
import com.relino.core.support.id.IdGenerator;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobProducer {

    private Store store;
    private IdGenerator idGenerator;

    public JobProducer(Store store, IdGenerator idGenerator) {
        this.store = store;
        this.idGenerator = idGenerator;
    }

    /**
     * 创建一个job
     *
     * @throws JobCreateException
     * @throws JobDuplicateException
     */
    public void createJob(Job job) throws JobCreateException, JobDuplicateException {
        try {
            store.insertJob(job);
        } catch (Exception e) {
            throw new JobCreateException("创建job失败", e, job);
        }
    }

    public JobBuilder builder(Oper mainOper) {
        String jobId = idGenerator.getNext();
        return new JobBuilder(jobId, mainOper);
    }

    public JobBuilder builder(String jobId, Oper mainOper) {
        return new JobBuilder(jobId, mainOper);
    }

    public static class JobBuilder {

        private final String jobId;
        private final Oper mOper;
        private String idempotentId;

        private String jobCode = Job.DEFAULT_JOB_CODE;
        private boolean delayJob = false;
        private LocalDateTime beginTime = LocalDateTime.now();
        private JobAttr commonAttr = new JobAttr();

        public JobBuilder(String jobId, Oper mOper) {
            this.jobId = jobId;
            this.mOper = mOper;
            this.idempotentId = jobId;
        }

        public JobBuilder idempotentId(String idempotentId) {
            this.idempotentId = idempotentId;
            return this;
        }

        public JobBuilder jobCode(String jobCode) {
            this.jobCode = jobCode;
            return this;
        }

        public JobBuilder delayJob(int delaySeconds) {
            this.delayJob = true;
            this.beginTime = LocalDateTime.now().plusSeconds(delaySeconds);
            return this;
        }

        public JobBuilder commonAttr(JobAttr commonAttr) {
            this.commonAttr = commonAttr;
            return this;
        }

        public Job build() {
            return new Job(this);
        }

        public String getJobId() {
            return jobId;
        }

        public Oper getMOper() {
            return mOper;
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
    }
}
