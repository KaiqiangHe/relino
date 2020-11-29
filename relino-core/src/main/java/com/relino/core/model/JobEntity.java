package com.relino.core.model;

import com.relino.core.support.Utils;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobEntity {

    private long id;
    private String jobId;
    private String idempotentId;
    private String jobCode;

    private int isDelayJob;
    private LocalDateTime beginTime;

    private String commonAttr;

    private LocalDateTime willExecuteTime ;
    private int jobStatus;
    private long executeOrder;

    private String mActionId;
    private int mOperStatus;
    private int mExecuteCount;
    private String mRetryPolicyId;
    private int mMaxRetry;

    private LocalDateTime createTime;

    public JobEntity() {
    }

    /**
     * 将JobEntity转换为Job对象
     */
    public static Job toJob(JobEntity entity) {
        if(entity == null) {
            return null;
        }

        Oper mOper = new Oper(
                entity.getMActionId(),
                OperStatus.toEnum(entity.getMOperStatus()),
                entity.getMExecuteCount(),
                entity.getMMaxRetry(),
                entity.getMRetryPolicyId()
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getIdempotentId() {
        return idempotentId;
    }

    public void setIdempotentId(String idempotentId) {
        this.idempotentId = idempotentId;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public int getIsDelayJob() {
        return isDelayJob;
    }

    public void setIsDelayJob(int isDelayJob) {
        this.isDelayJob = isDelayJob;
    }

    public LocalDateTime getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(LocalDateTime beginTime) {
        this.beginTime = beginTime;
    }

    public String getCommonAttr() {
        return commonAttr;
    }

    public void setCommonAttr(String commonAttr) {
        this.commonAttr = commonAttr;
    }

    public LocalDateTime getWillExecuteTime() {
        return willExecuteTime;
    }

    public void setWillExecuteTime(LocalDateTime willExecuteTime) {
        this.willExecuteTime = willExecuteTime;
    }

    public int getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(int jobStatus) {
        this.jobStatus = jobStatus;
    }

    public long getExecuteOrder() {
        return executeOrder;
    }

    public void setExecuteOrder(long executeOrder) {
        this.executeOrder = executeOrder;
    }

    public String getMActionId() {
        return mActionId;
    }

    public void setMActionId(String mActionId) {
        this.mActionId = mActionId;
    }

    public int getMOperStatus() {
        return mOperStatus;
    }

    public void setMOperStatus(int mOperStatus) {
        this.mOperStatus = mOperStatus;
    }

    public int getMExecuteCount() {
        return mExecuteCount;
    }

    public void setMExecuteCount(int mExecuteCount) {
        this.mExecuteCount = mExecuteCount;
    }

    public String getMRetryPolicyId() {
        return mRetryPolicyId;
    }

    public void setMRetryPolicyId(String mRetryPolicyId) {
        this.mRetryPolicyId = mRetryPolicyId;
    }

    public int getMMaxRetry() {
        return mMaxRetry;
    }

    public void setMMaxRetry(int mMaxRetry) {
        this.mMaxRetry = mMaxRetry;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
