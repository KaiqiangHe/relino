package com.relino.core;

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
import java.util.Arrays;

/**
 * @author kaiqiang.he
 */
public class Relino {

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;
    private static final String CONNECT_STR = "127.0.0.1:2181";
    private static final int DEFAULT_WATCH_DOG_PER_SECOND = 1;


    public final DataSource dataSource;
    public final DBExecutor dbExecutor;
    public final JobStore jobStore;
    public final IdGenerator idGenerator;
    public final QueueSizeLimitExecutor<Job> jotExecutor;
    public final ExecuteQueue executeQueue;
    public final JobProducer jobProducer;
    public final JobProcessor jobProcessor;
    public final PullExecutableJobAndExecute pullExecutableJobAndExecute;

    public final int pullJobBatchSize;
    public final int scanRunnableJobBatchSize;
    public final CuratorFramework curatorClient;
    public final CuratorLeaderElection curatorLeaderElection;

    public Relino(DataSource dataSource, int pullJobBatchSize, int scanRunnableJobBatchSize, int watchDogMinutes) {
        this.dataSource = dataSource;
        this.dbExecutor = new DBExecutor(dataSource);
        this.jobStore = new JobStore(this.dbExecutor);

        this.idGenerator = new UUIDIdGenerator();
        this.jobProcessor = new JobProcessor(jobStore);
        this.jotExecutor = new QueueSizeLimitExecutor<Job>("job", 5, 20, 3000, jobProcessor);
        this.executeQueue = new PessimisticLockExecuteQueue(dbExecutor);
        this.pullJobBatchSize = pullJobBatchSize;
        this.scanRunnableJobBatchSize = scanRunnableJobBatchSize;
        this.pullExecutableJobAndExecute = new PullExecutableJobAndExecute(pullJobBatchSize, DEFAULT_WATCH_DOG_PER_SECOND, executeQueue, jotExecutor);

        this.jobProducer = new JobProducer(jobStore, idGenerator);

        // 创建Curator Client
        RetryPolicy retryPolicy = new RetryNTimes(10, 100);
        curatorClient = CuratorFrameworkFactory.newClient(
                CONNECT_STR, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();

        // 创建curatorLeaderElection
        RelinoLeaderElectionListener scanRunnableDelayJobListener = new RelinoLeaderElectionListener(
                "scanRunnableDelayJob",
                "/relino/scanRunnableDelayJob",
                () -> new ScanRunnableDelayJob(dbExecutor, 100));

        RelinoLeaderElectionListener deadJobWatchDog = new RelinoLeaderElectionListener(
                "deadJobWatchDog",
                "/relino/deadJobWatchDog",
                () -> new DeadJobWatchDog(dbExecutor, watchDogMinutes)
        );

        curatorLeaderElection = new CuratorLeaderElection(Arrays.asList(scanRunnableDelayJobListener, deadJobWatchDog), curatorClient);
        curatorLeaderElection.execute();
    }
}
