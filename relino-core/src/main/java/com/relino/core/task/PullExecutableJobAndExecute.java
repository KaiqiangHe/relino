package com.relino.core.task;

import com.relino.core.model.executequeue.ExecuteQueue;
import com.relino.core.model.Job;
import com.relino.core.support.AbstractRunSupport;
import com.relino.core.support.Utils;
import com.relino.core.support.thread.QueueSizeLimitExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 获取可执行的job并提交执行
 * // TODO: 2020/11/22  test
 *
 * @author kaiqiang.he
 */
public final class PullExecutableJobAndExecute extends AbstractRunSupport {

    private static final Logger log = LoggerFactory.getLogger(PullExecutableJobAndExecute.class);

    private int pullSize;
    private final ExecuteQueue executeQueue;
    private final QueueSizeLimitExecutor<Job> executor;

    public PullExecutableJobAndExecute(int pullSize, ExecuteQueue executeQueue, QueueSizeLimitExecutor<Job> executor) {
        this.pullSize = pullSize;
        this.executeQueue = executeQueue;
        this.executor = executor;
    }

    @Override
    protected void execute0() {
        try {
            while (true) {

                if(wannaStop()) { break; }

                List<Job> jobs = null;
                try {
                    jobs = executeQueue.getNextExecutableJob(pullSize);
                } catch (Exception e) {
                    log.error("getNextExecutableJob error ", e);
                }

                boolean isWait;
                if(!Utils.isEmpty(jobs)) {
                    isWait = false;
                    int index = 0;
                    while(index < jobs.size()) {
                        Job job = jobs.get(index);
                        if(executor.addItem(job, 50, TimeUnit.MICROSECONDS)) {
                            index++;
                        }
                    }
                } else {
                    isWait = true;
                }

                if(wannaStop()) { break; }

                if(isWait) {
                    Thread.sleep(500);
                }
            }
        } catch (Exception e) {
            log.error("PullExecutableJobAndExecute error ", e);
        }
    }
}
