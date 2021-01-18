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
import com.relino.core.task.PullExecutableJobAndExecute;
import com.relino.core.task.ScanRunnableDelayJob;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author kaiqiang.he
 */
public class Relino {

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;
    private static final int DEFAULT_WATCH_DOG_PER_SECOND = 1;

    public static final String DEFAULT_RETRY_POLICY = "_df";
    public static final String IMMEDIATELY_RETRY_POLICY = "_im";

    public final RelinoConfig relinoConfig;

    public final DBExecutor dbExecutor;
    public final JobStore jobStore;
    public final JobFactory jobFactory;
    public final QueueSizeLimitExecutor<Job> jobExecutor;
    public final JobProcessor jobProcessor;
    public final RunnableExecuteQueue runnableExecuteQueue;
    public final PullExecutableJobAndExecute pullExecutableJobAndExecute;

    public final BeanManager<Action> actionManager = new BeanManager<>();
    public final BeanManager<IRetryPolicy> retryPolicyBeanManager = new BeanManager<>();

    public final CuratorFramework curatorClient;
    public final CuratorLeaderElection curatorLeaderElection;

    public Relino(RelinoConfig relinoConfig) {
        this.relinoConfig = relinoConfig;

        // 注册Action
        relinoConfig.getActionMap().forEach(actionManager::register);

        // 注册默认重试策略
        retryPolicyBeanManager.register(DEFAULT_RETRY_POLICY, relinoConfig.getDefaultRetryPolicy());
        retryPolicyBeanManager.register(IMMEDIATELY_RETRY_POLICY, new ImmediatelyRetryPolicy());
        // 注册自定义重试策略
        relinoConfig.getSelfRetryPolicy().forEach(retryPolicyBeanManager::register);

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
                this,
                relinoConfig.getPullRunnableJobBatchSize(),
                DEFAULT_WATCH_DOG_PER_SECOND,
                runnableExecuteQueue,
                jobExecutor);

        this.jobFactory = new JobFactory(jobStore, relinoConfig.getIdGenerator(), actionManager, retryPolicyBeanManager);

        // 主节点选取
        // 创建Curator Client
        RetryPolicy retryPolicy = new RetryNTimes(10, 100);
        curatorClient = CuratorFrameworkFactory.newClient(
                relinoConfig.getZkConnectStr(), SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();

        relinoConfig.registerLeaderSelector(new LeaderSelectorConfig("scanRunnableDelayJob",
                "/relino/scanRunnableDelayJob",
                () -> new ScanRunnableDelayJob(dbExecutor, relinoConfig.getScanRunnableJobBatchSize())));

        relinoConfig.registerLeaderSelector(
                new LeaderSelectorConfig("deadJobWatchDog",
                        "/relino/deadJobWatchDog",
                        () -> new DeadJobWatchDog(dbExecutor, relinoConfig.getWatchDogTimeOutMinutes()))

        );
        List<RelinoLeaderElectionListener> electionListenerList =
                relinoConfig.getLeaderSelectorConfigList().stream().map(RelinoLeaderElectionListener::new).collect(Collectors.toList());
        curatorLeaderElection = new CuratorLeaderElection(electionListenerList, curatorClient);
        curatorLeaderElection.execute();
    }
}
