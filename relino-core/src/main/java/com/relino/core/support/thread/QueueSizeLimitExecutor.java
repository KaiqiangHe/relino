package com.relino.core.support.thread;

import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通过BlockingQueue实现生产者消费者模型，避免添加的速度过快
 *
 * @author kaiqiang.he
 */
public class QueueSizeLimitExecutor<T extends Processor> implements Runnable {
    
    private static final Logger log = LoggerFactory.getLogger(QueueSizeLimitExecutor.class);
    
    private static final int DEFAULT_QUEUE_SIZE = 1000;

    private final String name;
    private final int coreThread;
    private final int maxThread;
    private final int queueSize;

    private BlockingQueue<T> queue;
    private ThreadPoolExecutor workers;

    public QueueSizeLimitExecutor(String name, int coreThread, int maxThread, int queueSize) {

        Utils.check(name, Utils::isEmpty, "name不能为空");
        Utils.check(coreThread, v -> v <= 0, "coreThread应大于0, [" + coreThread + "]");
        Utils.check(maxThread, v -> v <= 0 || maxThread < coreThread, "coreThread应大于0且大于等于maxThread, [" + coreThread + "," + maxThread + "]");
        Utils.check(queueSize, v -> v <= 0, "queueSize必须大于0");

        this.name = name;
        this.coreThread = coreThread;
        this.maxThread = maxThread;
        this.queueSize = queueSize;

        init();
    }

    public QueueSizeLimitExecutor(String name, int coreThread, int maxThread) {
        this(name, coreThread, maxThread, DEFAULT_QUEUE_SIZE);
    }

    private void init() {
        queue = new ArrayBlockingQueue<>(queueSize);

        // workQueue 的大小必须为1
        // 拒绝策略必须为 DiscardPolicy 直接丢弃
        workers = new ThreadPoolExecutor(
                coreThread,
                maxThread,
                1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1),
                new NamedThreadFactory("job-execute-" + name + "-" , true),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public boolean addItem(T job) {
        boolean offered = queue.offer(job);
        if(offered) {
            workers.execute(this);
        }
        return offered;
    }

    public boolean addItem(T job, long timeout, TimeUnit unit) throws InterruptedException {
        boolean offered = queue.offer(job, timeout, unit);
        if(offered) {
            workers.execute(this);
        }
        return offered;
    }

    @Override
    public void run() {
        try {
            while (!queue.isEmpty()) {
                T elem = queue.poll();
                if(elem == null) {
                    return ;
                }

                elem.process();
            }
        } catch (Exception e) {
            log.error("execute error ", e);
        }
    }
}
