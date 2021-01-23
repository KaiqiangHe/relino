package com.relino.core.task;

import com.relino.core.Relino;
import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.model.executequeue.RunnableExecuteQueue;
import com.relino.core.support.Utils;
import com.relino.core.support.thread.NamedThreadFactory;
import com.relino.core.support.thread.QueueSizeLimitExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 获取可执行的job并提交执行
 *
 * @author kaiqiang.he
 */
public final class PullExecutableJobAndExecute {

    private static final Logger log = LoggerFactory.getLogger(PullExecutableJobAndExecute.class);

    private final ScheduledExecutorService watchExecutor =
            Executors.newScheduledThreadPool(1, new NamedThreadFactory("relino-dead-job-watchdog", true));
    private ScheduledFuture<?> watchFuture;

    private Relino relino;
    private int pullSize;
    private int watchPerSeconds;
    private final RunnableExecuteQueue executeQueue;
    private final QueueSizeLimitExecutor<Job> executor;

    public PullExecutableJobAndExecute(Relino relino,
                                       int pullSize,
                                       int watchPerSeconds,
                                       RunnableExecuteQueue executeQueue,
                                       QueueSizeLimitExecutor<Job> executor) {

        Utils.checkNoNull(relino);
        Utils.check(pullSize, p -> p <= 0, "参数pullSize应该大于0, current = " + pullSize);
        Utils.check(watchPerSeconds, p -> p <= 0, "参数watchPerSeconds应该大于0, current = " + watchPerSeconds);

        this.relino = relino;
        this.pullSize = pullSize;
        this.watchPerSeconds = watchPerSeconds;
        this.executeQueue = executeQueue;
        this.executor = executor;
    }

    public void start() {
        watchFuture = watchExecutor.scheduleAtFixedRate(() -> {
            log.info("PullExecutableJobAndExecute开始运行");
            try {
                pullJob();
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
            }
            log.info("PullExecutableJobAndExecute运行结束");
        }, 0, this.watchPerSeconds, TimeUnit.SECONDS);
    }

    protected void pullJob() throws Exception {
        long start = System.currentTimeMillis();
        List<Job> jobs = executeQueue.getNextRunnableJob(pullSize);
        while (!Utils.isEmpty(jobs)) {
            int index = 0;
            while(index < jobs.size()) {
                Job job = jobs.get(index);
                job.setRelino(relino);
                if(executor.addItem(job, 50, TimeUnit.MICROSECONDS)) {
                    index++;
                }
            }

            jobs = executeQueue.getNextRunnableJob(pullSize);
        }
        if(log.isDebugEnabled()) {
            log.debug("pull job ids = [{}], time = {}",
                    jobs.stream().map(Job::getJobId).collect(Collectors.joining(",")),
                    System.currentTimeMillis() - start);
        }
    }

    public void destroy() {
        if(watchFuture != null && !watchFuture.isCancelled()) {
            watchFuture.cancel(false);
        }
    }
}
