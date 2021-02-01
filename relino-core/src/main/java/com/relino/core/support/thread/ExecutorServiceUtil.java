package com.relino.core.support.thread;

import java.util.concurrent.ScheduledFuture;

/**
 * @author kaiqiang.he
 */
public class ExecutorServiceUtil {

    public static void cancelScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
    }
}
