package com.relino.core.model;

import com.relino.core.JobProducer.JobBuilder;
import com.relino.core.support.Utils;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class BaseJob implements Job {

    public static final String DEFAULT_JOB_CODE = Utils.EMPTY_STRING;

    public static final int DELAY_EXECUTE_ORDER = -1;

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
     * 当前job将要执行时间
     */
    private LocalDateTime willExecuteTime;

    @Override
    public void delayExecute(LocalDateTime nextExecuteTime) {
        LocalDateTime now = LocalDateTime.now();
        if(LocalDateTime.now().minusMinutes(5).isAfter(nextExecuteTime)) {
            throw new RuntimeException("延迟执行时间与当前时间不应超过5分钟, 当前时间[" + Utils.toStrDate(now) +
                    ", 延迟执行时间[" + Utils.toStrDate(nextExecuteTime));
        }

        this.jobStatus = JobStatus.SLEEP;
        this.executeOrder = DELAY_EXECUTE_ORDER;
        this.willExecuteTime = nextExecuteTime;
    }

    @Override
    public void finished() {
        this.jobStatus = JobStatus.FINISHED;
    }

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
     * mainOper
     */
    private final Oper mOper;

    public BaseJob(Long id, String jobId, String idempotentId, String jobCode,
                   boolean delayJob, LocalDateTime beginTime,
                   JobStatus jobStatus, long executeOrder, LocalDateTime willExecuteTime,
                   JobAttr commonAttr,

                   Oper mOper) {
        this.id = id;
        this.jobId = jobId;
        this.idempotentId = idempotentId;
        this.jobCode = jobCode;


        this.delayJob = delayJob;
        this.beginTime = beginTime;

        this.jobStatus = jobStatus;
        this.executeOrder = executeOrder;
        this.willExecuteTime = willExecuteTime;

        this.commonAttr = commonAttr;
        this.mOper = mOper;
    }

    public BaseJob(JobBuilder builder) {
        this.id = null;
        this.jobId = builder.getJobId();
        this.idempotentId = builder.getIdempotentId();
        this.jobCode = builder.getJobCode();;
        this.delayJob = builder.isDelayJob();
        this.beginTime = builder.getBeginTime();
        this.commonAttr = builder.getCommonAttr();
        this.mOper = builder.getMOper();

        delayExecute(this.beginTime);
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

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", jobId='" + jobId + '\'' +
                ", idempotentId='" + idempotentId + '\'' +
                ", jobCode='" + jobCode + '\'' +
                ", jobStatus=" + jobStatus +
                ", executeOrder=" + executeOrder +
                '}';
    }
}
