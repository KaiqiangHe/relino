package com.relino.core.model;

import com.relino.core.JobBuilder;
import com.relino.core.Relino;
import com.relino.core.exception.RelinoException;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.bean.BeanWrapper;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class Job {

    public static final String DEFAULT_JOB_CODE = Utils.EMPTY_STRING;

    public static final int DELAY_EXECUTE_ORDER = -1;

    /**
     * // TODO: 2021/1/16
     */
    private Relino relino;

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
     * attr
     */
    private final JobAttr commonAttr;

    /**
     * mainOper
     */
    private final Oper mOper;

    /**
     * // TODO: 2021/1/16  delete
     */
    public Job(Long id, String jobId, String idempotentId, String jobCode,
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

    public Job(JobBuilder builder) {

        this.id = null;
        this.jobId = builder.getJobId();
        this.idempotentId = builder.getIdempotentId();
        this.jobCode = builder.getJobCode();;
        this.delayJob = builder.isDelayJob();
        this.beginTime = builder.getBeginTime();
        this.commonAttr = builder.getCommonAttr();
        delayExecute(this.beginTime);

        Utils.checkNonEmpty(this.jobId);
        Utils.checkNonEmpty(this.idempotentId);
        Utils.checkNoNull(this.jobCode);
        Utils.checkNoNull(this.beginTime);
        Utils.checkNoNull(this.commonAttr);

        this.mOper = new Oper(builder);
    }

    /**
     * 设置job延迟执行
     */
    public void delayExecute(LocalDateTime delayExecuteTime) {
        LocalDateTime now = LocalDateTime.now();
        if(LocalDateTime.now().minusMinutes(5).isAfter(delayExecuteTime)) {
            throw new RuntimeException("延迟执行时间与当前时间不应超过5分钟, 当前时间[" + Utils.toStrDate(now) +
                    ", 延迟执行时间[" + Utils.toStrDate(delayExecuteTime));
        }

        this.jobStatus = JobStatus.DELAY;
        this.executeOrder = DELAY_EXECUTE_ORDER;
        this.willExecuteTime = delayExecuteTime;
    }

    public void finished() {
        this.jobStatus = JobStatus.FINISHED;
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

    public Relino getRelino() {
        return relino;
    }

    public void setRelino(Relino relino) {
        this.relino = relino;
    }

    // ------------------------------------------------------------------------------

    public static class Oper {
        /**
         * 默认最大重试次数
         */
        public static final int DEFAULT_MAX_RETRY_COUNT = 3;

        private final BeanWrapper<Action> action;

        /**
         * 当前oper的状态
         */
        private OperStatus operStatus;

        /**
         * 已执行次数
         */
        private int executeCount;

        // ---------------------------------------------------
        // 重试
        /**
         * 最大重试次数
         */
        private final int maxExecuteCount;

        /**
         * 重试策略
         */
        private final BeanWrapper<IRetryPolicy> retryPolicy;

        public Oper(BeanWrapper<Action> action, OperStatus operStatus, int executeCount, int maxExecuteCount, BeanWrapper<IRetryPolicy> retryPolicy) {

            this.action = action;
            this.operStatus = operStatus;
            this.executeCount = executeCount;
            this.maxExecuteCount = maxExecuteCount;
            this.retryPolicy = retryPolicy;

            Utils.checkNoNull(this.action);
            Utils.checkNoNull(retryPolicy);

            if(this.maxExecuteCount < 1) {
                throw new RelinoException("Parameter 'maxExecuteCount' should >=1, actual = " + this.maxExecuteCount);
            }
        }

        public Oper(JobBuilder builder) {
            this(builder.getAction(), OperStatus.UN_FINISHED, 0, builder.getMaxExecuteCount(), builder.getRetryPolicy());
        }

        /**
         * 执行当前oper
         *
         * @return job not null, 执行结果
         */
        public ActionResult execute(String jobId, JobAttr commonAttr) {

            executeCount ++;
            ActionResult ret = action.getBean().execute(jobId, commonAttr, executeCount);

            if(ret.isSuccess()) {
                operStatus = OperStatus.SUCCESS_FINISHED;
            } else {
                if(executeCount >= maxExecuteCount) {
                    operStatus = OperStatus.FAILED_FINISHED;
                }
            }

            return ret;
        }

        /**
         * 获取延迟执行的时间, 如果为null 立即重试
         */
        public LocalDateTime getRetryExecuteTime() {

            int delaySeconds = retryPolicy.getBean().retryAfterSeconds(executeCount);
            if(delaySeconds == 0) {
                return null;
            } else {
                return LocalDateTime.now().plusSeconds(delaySeconds);
            }
        }

        /**
         * 执行是否结束
         */
        public boolean isExecuteFinished() {
            return operStatus == OperStatus.SUCCESS_FINISHED || operStatus == OperStatus.FAILED_FINISHED;
        }

        public OperStatus getOperStatus() {
            return operStatus;
        }

        public int getExecuteCount() {
            return executeCount;
        }

        public int getMaxExecuteCount() {
            return maxExecuteCount;
        }

        public BeanWrapper<Action> getAction() {
            return action;
        }

        public BeanWrapper<IRetryPolicy> getRetryPolicy() {
            return retryPolicy;
        }
    }
}
