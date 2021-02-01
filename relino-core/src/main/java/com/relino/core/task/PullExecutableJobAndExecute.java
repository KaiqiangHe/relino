package com.relino.core.task;

import com.relino.core.Relino;
import com.relino.core.exception.HandleException;
import com.relino.core.model.Job;
import com.relino.core.model.executequeue.RunnableExecuteQueue;
import com.relino.core.support.Utils;
import com.relino.core.support.thread.ExecutorServiceUtil;
import com.relino.core.support.thread.NamedThreadFactory;
import com.relino.core.support.thread.QueueSizeLimitExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
    private final RunnableExecuteQueue executeQueue;
    private final QueueSizeLimitExecutor<Job> executor;

    public PullExecutableJobAndExecute(Relino relino,
                                       int pullSize,
                                       RunnableExecuteQueue executeQueue,
                                       QueueSizeLimitExecutor<Job> executor) {

        Utils.checkNoNull(relino);
        Utils.check(pullSize, p -> p <= 0, "参数pullSize应该大于0, current = " + pullSize);

        this.relino = relino;
        this.pullSize = pullSize;
        this.executeQueue = executeQueue;
        this.executor = executor;
    }

    public void start() {
        watchFuture = watchExecutor.scheduleAtFixedRate(() -> {
            try {
                pullJob();
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
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
        log.info("pull executable job ids = [{}], time = {}",
                jobs.stream().map(Job::getJobId).collect(Collectors.joining(",")),
                System.currentTimeMillis() - start);
    }

    public void destroy() {
        try {
            ExecutorServiceUtil.cancelScheduledFuture(watchFuture);
        } catch (Exception e) {
            HandleException.handleUnExpectedException(e);
        }

        watchExecutor.shutdown();
    }
}
