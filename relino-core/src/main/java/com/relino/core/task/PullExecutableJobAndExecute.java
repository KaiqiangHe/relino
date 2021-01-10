package com.relino.core.task;

import com.relino.core.exception.HandleException;
import com.relino.core.model.executequeue.ExecuteQueue;
import com.relino.core.model.BaseJob;
import com.relino.core.support.Utils;
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
            Executors.newScheduledThreadPool(1, new NamedThreadFactory("DeadJobWatchDog", true));
    private final ScheduledFuture<?> watchFuture;

    private int pullSize;
    private final ExecuteQueue executeQueue;
    private final QueueSizeLimitExecutor<BaseJob> executor;

    public PullExecutableJobAndExecute(int pullSize, int watchPerSeconds, ExecuteQueue executeQueue, QueueSizeLimitExecutor<BaseJob> executor) {

        Utils.check(pullSize, p -> p <= 0, "参数pullSize应该大于0, current = " + pullSize);
        Utils.check(watchPerSeconds, p -> p <= 0, "参数watchPerSeconds应该大于0, current = " + watchPerSeconds);

        this.pullSize = pullSize;
        this.executeQueue = executeQueue;
        this.executor = executor;

        watchFuture = watchExecutor.scheduleAtFixedRate(() -> {
            log.info("PullExecutableJobAndExecute开始运行");
            try {
                pullJob();
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
            }
            log.info("PullExecutableJobAndExecute运行结束");
        }, 0, watchPerSeconds, TimeUnit.SECONDS);
    }

    protected void pullJob() throws Exception {
        long start = System.currentTimeMillis();
        List<BaseJob> jobs = executeQueue.getNextExecutableJob(pullSize);
        while (!Utils.isEmpty(jobs)) {
            int index = 0;
            while(index < jobs.size()) {
                BaseJob job = jobs.get(index);
                if(executor.addItem(job, 50, TimeUnit.MICROSECONDS)) {
                    index++;
                }
            }
            jobs = executeQueue.getNextExecutableJob(pullSize);
        }
        if(log.isDebugEnabled()) {
            log.debug("pull job ids = [{}], time = {}",
                    jobs.stream().map(BaseJob::getJobId).collect(Collectors.joining(",")),
                    System.currentTimeMillis() - start);
        }
    }
}
