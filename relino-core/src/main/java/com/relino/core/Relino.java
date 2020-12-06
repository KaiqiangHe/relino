package com.relino.core;

import com.relino.core.db.Store;
import com.relino.core.model.Job;
import com.relino.core.model.executequeue.ExecuteQueue;
import com.relino.core.model.executequeue.PessimisticLockExecuteQueue;
import com.relino.core.register.CuratorLeaderElection;
import com.relino.core.register.RelinoLeaderElectionListener;
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

import java.util.Arrays;

/**
 * @author kaiqiang.he
 */
public class Relino {

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;
    private static final String CONNECT_STR = "127.0.0.1:2181";

    public final Store store;
    public final IdGenerator idGenerator;
    public final QueueSizeLimitExecutor<Job> jotExecutor;
    public final ExecuteQueue executeQueue;
    public final JobProducer jobProducer;
    public final int pullJobBatchSize;
    public final int scanRunnableJobBatchSize;
    public final CuratorFramework curatorClient;
    public final CuratorLeaderElection curatorLeaderElection;

    public Relino(Store store, int pullJobBatchSize, int scanRunnableJobBatchSize, int watchDogMinutes) {
        this.store = store;
        this.idGenerator = new UUIDIdGenerator();
        this.jotExecutor = new QueueSizeLimitExecutor<>("job", 5, 20, 3000);
        this.executeQueue = new PessimisticLockExecuteQueue(store);
        this.pullJobBatchSize = pullJobBatchSize;
        this.scanRunnableJobBatchSize = scanRunnableJobBatchSize;


        this.jobProducer = new JobProducer(store, idGenerator);

        // 创建Curator Client
        RetryPolicy retryPolicy = new RetryNTimes(10, 100);
        curatorClient = CuratorFrameworkFactory.newClient(
                CONNECT_STR, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();

        // 创建curatorLeaderElection
        RelinoLeaderElectionListener scanRunnableDelayJobListener = new RelinoLeaderElectionListener(
                "scanRunnableDelayJob",
                "/relino/scanRunnableDelayJob",
                () -> new ScanRunnableDelayJob(store, 100));

        RelinoLeaderElectionListener pullExecutableJobListener = new RelinoLeaderElectionListener(
                "pullExecutableJob",
                "/relino/pullExecutableJob",
                () -> new PullExecutableJobAndExecute(pullJobBatchSize, executeQueue, this.jotExecutor)
        );

        RelinoLeaderElectionListener deadJobWatchDog = new RelinoLeaderElectionListener(
                "deadJobWatchDog",
                "/relino/deadJobWatchDog",
                () -> new DeadJobWatchDog(store, watchDogMinutes)
        );

        curatorLeaderElection = new CuratorLeaderElection(Arrays.asList(scanRunnableDelayJobListener, pullExecutableJobListener, deadJobWatchDog), curatorClient);
        curatorLeaderElection.execute();
    }
}
