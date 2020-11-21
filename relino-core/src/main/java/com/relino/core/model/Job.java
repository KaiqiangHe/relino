package com.relino.core.model;

import com.relino.core.JobProducer;
import com.relino.core.JobProducer.JobBuilder;
import com.relino.core.support.Utils;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class Job {

    public static final String DEFAULT_JOB_CODE = Utils.EMPTY_STRING;

    /**
     * 数据库自动生成的id
     */
    private final Long id;

    /**
     * 全局唯一
     */
    private final String jobId;

    /**
     * 幂等字段, 默认为jobId
     */
    private final String idempotentId;

    /**
     * 可重复
     */
    private final String jobCode;

    // -------------------------------------------------------------
    private JobStatus jobStatus;

    private long executeOrder;

    /**
     * 当前job将要执行时间, 等于 mOper 或 cOper 的 willExecuteTime
     */
    private LocalDateTime willExecuteTime;

    // ------------------------------------------------------------
    /**
     * 是否为延迟job
     */
    private final boolean delayJob;
    /**
     * 延迟job开始执行时间
     */
    private final LocalDateTime beginTime;

    // -------------------------------------------------------------
    /**
     * 共享attr
     */
    private final JobAttr commonAttr;

    /**
     * mainOper, 不能为emptyAction
     */
    private final Oper mOper;

    public Job(Long id, String jobId, String idempotentId, String jobCode, boolean delayJob, LocalDateTime beginTime, JobAttr commonAttr, Oper mOper) {
        this.id = id;
        this.jobId = jobId;
        this.idempotentId = idempotentId;
        this.jobCode = jobCode;
        this.delayJob = delayJob;
        this.beginTime = beginTime;
        this.commonAttr = commonAttr;
        this.mOper = mOper;
    }

    public Job(JobBuilder builder) {
        this.id = null;
        this.jobId = builder.getJobId();
        this.idempotentId = builder.getIdempotentId();
        this.jobCode = builder.getJobCode();;
        this.delayJob = builder.isDelayJob();
        this.beginTime = builder.getBeginTime();
        this.willExecuteTime = this.beginTime;
        this.commonAttr = builder.getCommonAttr();
        this.mOper = builder.getMOper();
        this.jobStatus = JobStatus.DELAY;
        this.executeOrder = -1; // TODO: 2020/11/20
        this.mOper.setOperStatus(OperStatus.RUNNABLE);
    }

    // ------------------------------------------------------

    public Long getId() {
        return id;
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

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public long getExecuteOrder() {
        return executeOrder;
    }

    public LocalDateTime getWillExecuteTime() {
        return willExecuteTime;
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

    public Oper getMOper() {
        return mOper;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public void setExecuteOrder(long executeOrder) {
        this.executeOrder = executeOrder;
    }

    public void setWillExecuteTime(LocalDateTime willExecuteTime) {
        this.willExecuteTime = willExecuteTime;
    }
}
