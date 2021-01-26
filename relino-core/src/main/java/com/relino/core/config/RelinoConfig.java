package com.relino.core.config;

import com.relino.core.model.Action;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.model.retry.LinearRetryPolicy;
import com.relino.core.support.Utils;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.TimeHostPidIdGenerator;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kaiqiang.he
 */
public class RelinoConfig {

    private static final int DEFAULT_RETRY_K = 5;

    private String appId;

    // zk相关
    private String zkConnectStr;
    private List<LeaderSelectorConfig> leaderSelectorConfigList = new ArrayList<>();

    // 数据源
    private DataSource dataSource;

    // 执行job配置
    private int executorJobCoreThreadNum = 5;
    private int executorJobMaxThreadNum = 20;
    private int executorJobQueueSize = 2000;

    private int pullRunnableJobBatchSize = 200;
    private int scanRunnableJobBatchSize = 200;
    private int watchDogTimeOutMinutes = 5;

    private IdGenerator idGenerator = new TimeHostPidIdGenerator();

    // 默认重试策略
    private IRetryPolicy defaultRetryPolicy = new LinearRetryPolicy(DEFAULT_RETRY_K);

    // 自定义重试策略
    private Map<String, IRetryPolicy> selfRetryPolicy = new HashMap<>();

    // Action
    private Map<String, Action> actionMap = new HashMap<>();

    public RelinoConfig(String appId, String zkConnectStr, DataSource dataSource) {

        Utils.checkNoNull(appId);
        Utils.checkNoNull(zkConnectStr);
        Utils.checkNoNull(dataSource);

        this.appId = appId;
        this.zkConnectStr = zkConnectStr;
        this.dataSource = dataSource;
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

    public IRetryPolicy getDefaultRetryPolicy() {
        return defaultRetryPolicy;
    }

    public void setDefaultRetryPolicy(IRetryPolicy defaultRetryPolicy) {
        this.defaultRetryPolicy = defaultRetryPolicy;
    }

    public void registerRetryPolicy(String retryPolicyId, IRetryPolicy retry) {
        selfRetryPolicy.put(retryPolicyId, retry);
    }

    public Map<String, IRetryPolicy> getSelfRetryPolicy() {
        return selfRetryPolicy;
    }

    public Map<String, Action> getActionMap() {
        return actionMap;
    }

    public void registerAction(String actionId, Action action) {
        actionMap.put(actionId, action);
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }
}
