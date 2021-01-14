package com.relino.core;

import com.relino.core.config.LeaderSelectorConfig;
import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Job;
import com.relino.core.model.executequeue.ExecuteQueue;
import com.relino.core.model.executequeue.PessimisticLockExecuteQueue;
import com.relino.core.register.CuratorLeaderElection;
import com.relino.core.register.RelinoLeaderElectionListener;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.UUIDIdGenerator;
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

    public final RelinoConfig relinoConfig;

    public final DBExecutor dbExecutor;
    public final JobStore jobStore;
    public final IdGenerator idGenerator;
    public final QueueSizeLimitExecutor<Job> jobExecutor;
    public final ExecuteQueue executeQueue;
    public final JobProducer jobProducer;
    public final JobProcessor jobProcessor;
    public final PullExecutableJobAndExecute pullExecutableJobAndExecute;

    public final CuratorFramework curatorClient;
    public final CuratorLeaderElection curatorLeaderElection;

    public Relino(RelinoConfig relinoConfig) {
        this.relinoConfig = relinoConfig;

        DataSource dataSource = relinoConfig.getDataSource();
        this.dbExecutor = new DBExecutor(dataSource);
        this.jobStore = new JobStore(this.dbExecutor);

        this.idGenerator = new UUIDIdGenerator();
        this.jobProcessor = new JobProcessor(jobStore);
        this.jobExecutor = new QueueSizeLimitExecutor<Job>(
                "job",
                relinoConfig.getExecutorJobCoreThreadNum(),
                relinoConfig.getExecutorJobMaxThreadNum(),
                relinoConfig.getExecutorJobQueueSize(),
                jobProcessor);

        this.executeQueue = new PessimisticLockExecuteQueue(dbExecutor);
        this.pullExecutableJobAndExecute = new PullExecutableJobAndExecute(
                relinoConfig.getPullRunnableJobBatchSize(),
                DEFAULT_WATCH_DOG_PER_SECOND,
                executeQueue,
                jobExecutor);

        this.jobProducer = new JobProducer(jobStore, idGenerator);

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
