package com.relino.core.model;

/**
 * @author kaiqiang.he
 */
public class JobEntity {

    private long id;
    private String jobId;
    private long executeOrder;

    public JobEntity() {
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

    public long getExecuteOrder() {
        return executeOrder;
    }

    public void setExecuteOrder(long executeOrder) {
        this.executeOrder = executeOrder;
    }
}
