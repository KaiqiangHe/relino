package com.relino.core.model.db;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class ExecuteTimeEntity {

    private long id;
    private long executeOrder;
    private LocalDateTime executeJobTime;

    public ExecuteTimeEntity() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExecuteOrder() {
        return executeOrder;
    }

    public void setExecuteOrder(long executeOrder) {
        this.executeOrder = executeOrder;
    }

    public LocalDateTime getExecuteJobTime() {
        return executeJobTime;
    }

    public void setExecuteJobTime(LocalDateTime executeJobTime) {
        this.executeJobTime = executeJobTime;
    }
}
