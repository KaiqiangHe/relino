package com.relino.core.support.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author kaiqiang.he
 */
public class NamedThreadFactory implements ThreadFactory {
    
    private final AtomicInteger mThreadNum = new AtomicInteger(1);

    private String prefix;

    private boolean daemon;

    public NamedThreadFactory(String prefix, boolean daemon) {
        if(prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("prefix不能为空");
        }

        this.prefix = prefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + mThreadNum.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }
}
