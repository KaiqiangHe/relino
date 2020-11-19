package com.relino.core.model;

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
}
