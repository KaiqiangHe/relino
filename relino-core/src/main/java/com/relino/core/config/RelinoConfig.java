package com.relino.core.config;

import com.relino.core.support.Utils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public class RelinoConfig {

    private String appId;

    // zk相关
    private String zkConnectStr;
    private List<LeaderSelectorConfig> leaderSelectorConfigList;

    // 数据源
    private DataSource dataSource;

    // 执行job
    private int executorJobCoreThreadNum = 5;
    private int executorJobMaxThreadNum = 20;
    private int executorJobQueueSize = 3000;

    //
    private int pullRunnableJobBatchSize = 200;
    private int scanRunnableJobBatchSize = 200;
    private int watchDogTimeOutMinutes = 5;

    public RelinoConfig(String appId, String zkConnectStr, DataSource dataSource) {

        Utils.checkNoNull(appId);
        Utils.checkNoNull(zkConnectStr);
        Utils.checkNoNull(dataSource);

        this.appId = appId;
        this.zkConnectStr = zkConnectStr;
        this.dataSource = dataSource;

        leaderSelectorConfigList = new ArrayList<>();

    }

    public void registerLeaderSelector(LeaderSelectorConfig config) {
        this.leaderSelectorConfigList.add(config);
    }

    public List<LeaderSelectorConfig> getLeaderSelectorConfigList() {
        return leaderSelectorConfigList;
    }

    public String getAppId() {
        return appId;
    }

    public String getZkConnectStr() {
        return zkConnectStr;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setExecutorJobCoreThreadNum(int executorJobCoreThreadNum) {
        this.executorJobCoreThreadNum = executorJobCoreThreadNum;
    }

    public void setExecutorJobMaxThreadNum(int executorJobMaxThreadNum) {
        this.executorJobMaxThreadNum = executorJobMaxThreadNum;
    }

    public void setExecutorJobQueueSize(int executorJobQueueSize) {
        this.executorJobQueueSize = executorJobQueueSize;
    }

    public int getExecutorJobCoreThreadNum() {
        return executorJobCoreThreadNum;
    }

    public int getExecutorJobMaxThreadNum() {
        return executorJobMaxThreadNum;
    }

    public int getExecutorJobQueueSize() {
        return executorJobQueueSize;
    }

    public int getPullRunnableJobBatchSize() {
        return pullRunnableJobBatchSize;
    }

    public void setPullRunnableJobBatchSize(int pullRunnableJobBatchSize) {
        this.pullRunnableJobBatchSize = pullRunnableJobBatchSize;
    }

    public int getScanRunnableJobBatchSize() {
        return scanRunnableJobBatchSize;
    }

    public void setScanRunnableJobBatchSize(int scanRunnableJobBatchSize) {
        this.scanRunnableJobBatchSize = scanRunnableJobBatchSize;
    }

    public int getWatchDogTimeOutMinutes() {
        return watchDogTimeOutMinutes;
    }

    public void setWatchDogTimeOutMinutes(int watchDogTimeOutMinutes) {
        this.watchDogTimeOutMinutes = watchDogTimeOutMinutes;
    }
}
