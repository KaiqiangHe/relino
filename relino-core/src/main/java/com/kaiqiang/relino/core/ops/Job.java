package com.kaiqiang.relino.core.ops;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Job {

    private String jobId;

    private int jobStatus;

    /**
     * 开始执行时间, 默认为当前时间
     * // TODO: 2020/10/11 支持延迟执行
     */
    private LocalDateTime beginTime;

    private ReliableOper mainOper;

    private Oper onSuccess;

    private Oper onFailed;

    private String payload;

    private String mainOperResultExt;

    public void execute() {

    }
}
