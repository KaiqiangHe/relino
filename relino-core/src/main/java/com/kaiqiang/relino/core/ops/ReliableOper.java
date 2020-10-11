package com.kaiqiang.relino.core.ops;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 至少执行一次
 */
@Data
public class ReliableOper extends Oper {

    private static final int DEFAULT_MAX_RETRY_COUNT = 3;

    /**
     * 预期执行时间
     */
    private LocalDateTime willExecuteTime;

    /**
     * 当前状态
     */
    private int status;

    /**
     * 已执行次数
     */
    private int executeCount = 0;

    /**
     * 上一次执行时间
     */
    private LocalDateTime lastExecuteTime;

    /**
     * 最多重试次数
     */
    private int maxRetryCount;

    public ReliableOper(Execute execute, int maxRetryCount) {
        super(execute);
        this.maxRetryCount = maxRetryCount;
    }
}
