package com.relino.core.model;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobBuilder {

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
