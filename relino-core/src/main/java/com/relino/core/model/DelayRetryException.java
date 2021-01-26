package com.relino.core.model;

import com.relino.core.support.Utils;

import java.time.LocalDateTime;

/**
 * 延迟重试异常
 *
 * @author kaiqiang.he
 */
public class DelayRetryException extends RuntimeException {

    /**
     * 延迟n秒执行
     */
    private Integer delaySeconds;

    /**
     * 在当前时间点执行
     */
    private LocalDateTime delayExecuteTime;

    /**
     * @param delaySeconds >= 3
     */
    public DelayRetryException(Throwable cause, int delaySeconds) {
        super(cause);
        this.delaySeconds = delaySeconds;
    }

    public DelayRetryException(Throwable cause, LocalDateTime delayExecuteTime) {
        super(cause);
        this.delayExecuteTime = delayExecuteTime;
    }

    /**
     * @param delaySeconds >= 3
     */
    public DelayRetryException(String message, int delaySeconds) {
        super(message);
        this.delaySeconds = delaySeconds;
    }

    public DelayRetryException(String message, LocalDateTime delayExecuteTime) {
        super(message);
        this.delayExecuteTime = delayExecuteTime;
    }

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
    }

    public void setDelayExecuteTime(LocalDateTime delayExecuteTime) {
        this.delayExecuteTime = delayExecuteTime;
    }

    private void checkDelaySeconds(int delaySeconds) {
        if(delaySeconds < 3) {
            throw new IllegalStateException("delaySeconds必须>=3，当前为" + delaySeconds);
        }
    }

    private void checkDelayExecuteTime(LocalDateTime delayExecuteTime) {
        if(delayExecuteTime == null || delayExecuteTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("delayExecuteTime不为null并且时未来的一个时间，当前为" + Utils.toStrDate(delayExecuteTime));
        }
    }
}
