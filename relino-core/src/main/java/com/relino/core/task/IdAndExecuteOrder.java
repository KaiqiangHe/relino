package com.relino.core.task;

/**
 * @author kaiqiang.he
 */
public class IdAndExecuteOrder {
    private long id;
    private long executeOrder;

    public IdAndExecuteOrder(long id, long executeOrder) {
        this.id = id;
        this.executeOrder = executeOrder;
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
}
