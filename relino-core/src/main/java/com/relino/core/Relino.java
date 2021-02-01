package com.relino.core;

import com.relino.core.config.LeaderSelectorConfig;
import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Action;
import com.relino.core.model.Job;
import com.relino.core.model.JobConverter;
import com.relino.core.model.ObjectConverter;
import com.relino.core.model.db.JobEntity;
import com.relino.core.model.executequeue.PessimisticLockExecuteQueue;
import com.relino.core.model.executequeue.RunnableExecuteQueue;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.model.retry.ImmediatelyRetryPolicy;
import com.relino.core.register.CuratorLeaderElection;
import com.relino.core.register.RelinoLeaderElectionListener;
import com.relino.core.support.bean.BeanManager;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.thread.QueueSizeLimitExecutor;
import com.relino.core.task.DeadJobWatchDog;
import com.relino.core.task.JobFactory;
import com.relino.core.task.PullExecutableJobAndExecute;
import com.relino.core.task.ScanRunnableDelayJob;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author kaiqiang.he
 */
public class Relino {

    private static final Logger log = LoggerFactory.getLogger(Relino.class);

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;

    public static final String DEFAULT_RETRY_POLICY = "_df";
    public static final String IMMEDIATELY_RETRY_POLICY = "_im";

    private final RelinoConfig relinoConfig;

    private final String appId;
    public final DBExecutor dbExecutor;
    private final JobStore jobStore;
    private final JobFactory jobFactory;
    private final QueueSizeLimitExecutor<Job> jobExecutor;
    private final JobProcessor jobProcessor;
    private final RunnableExecuteQueue runnableExecuteQueue;
    private final PullExecutableJobAndExecute pullExecutableJobAndExecute;

    private final BeanManager<Action> actionManager = new BeanManager<>();
    private final BeanManager<IRetryPolicy> retryPolicyBeanManager = new BeanManager<>();

    // zk
    private final CuratorFramework curatorClient;
    private final CuratorLeaderElection curatorLeaderElection;

    public Relino(RelinoConfig relinoConfig) {
        this.relinoConfig = relinoConfig;

        this.appId = relinoConfig.getAppId();

        registerActionAndRetryPolicy();

        DataSource dataSource = relinoConfig.getDataSource();
        this.dbExecutor = new DBExecutor(dataSource);
        this.jobStore = new JobStore(this.dbExecutor);

        this.jobProcessor = new JobProcessor(jobStore);
        this.jobExecutor = new QueueSizeLimitExecutor<Job>(
                "job",
                relinoConfig.getExecutorJobCoreThreadNum(),
                relinoConfig.getExecutorJobMaxThreadNum(),
                relinoConfig.getExecutorJobQueueSize(),
                jobProcessor);

        ObjectConverter<JobEntity, Job> jobConverter = new JobConverter(actionManager, retryPolicyBeanManager);
        this.runnableExecuteQueue = new PessimisticLockExecuteQueue(dbExecutor, jobConverter);
        this.pullExecutableJobAndExecute = new PullExecutableJobAndExecute(
                relinoConfig.getPullRunnableJobBatchSize(),
                runnableExecuteQueue,
                jobExecutor);

        // 创建jobFactory
        this.jobFactory = new DefaultJobFactory(jobStore, relinoConfig.getIdGenerator(), actionManager, retryPolicyBeanManager);

        // 创建Curator Client
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(relinoConfig.getZkMaxRetries(), relinoConfig.getZkMaxRetrySleepMillSeconds());
        curatorClient = CuratorFrameworkFactory.newClient(
                relinoConfig.getZkConnectStr(), SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);

        // 主节点选取
        relinoConfig.registerLeaderSelector(
                new LeaderSelectorConfig("scanRunnableDelayJob", () -> new ScanRunnableDelayJob(dbExecutor, relinoConfig.getScanRunnableJobBatchSize()))
        );
        relinoConfig.registerLeaderSelector(
                new LeaderSelectorConfig("deadJobWatchDog", () -> new DeadJobWatchDog(dbExecutor, relinoConfig.getWatchDogTimeOutMinutes()))
        );
        List<RelinoLeaderElectionListener> electionListenerList =
                relinoConfig.getLeaderSelectorConfigList().stream().map(RelinoLeaderElectionListener::new).collect(Collectors.toList());
        curatorLeaderElection = new CuratorLeaderElection(appId, electionListenerList, curatorClient);
    }

    public void start() {
        //
        curatorClient.start();
        try {
            if (!curatorClient.blockUntilConnected(relinoConfig.getZkMaxRetries() * relinoConfig.getZkMaxRetrySleepMillSeconds(), TimeUnit.MILLISECONDS)) {
                curatorClient.close();
                throw new RuntimeException("启动zk失败");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        curatorLeaderElection.start();

        this.pullExecutableJobAndExecute.start();
    }

    /**
     *
     */
    public void shutdown() {
        try {
            pullExecutableJobAndExecute.destroy();
            jobExecutor.shutdown();
            curatorLeaderElection.close();
            curatorClient.close();
        } catch (Exception e) {
            log.error("结束Relino异常", e);
        }
    }

    public JobFactory getJobFactory() {
        return jobFactory;
    }

    private void registerActionAndRetryPolicy() {
        // 注册Action
        relinoConfig.getActionMap().forEach(actionManager::register);

        // 注册默认重试策略
        retryPolicyBeanManager.register(DEFAULT_RETRY_POLICY, relinoConfig.getDefaultRetryPolicy());
        retryPolicyBeanManager.register(IMMEDIATELY_RETRY_POLICY, new ImmediatelyRetryPolicy());
        // 注册自定义重试策略
        relinoConfig.getSelfRetryPolicy().forEach(retryPolicyBeanManager::register);
    }
}
