package com.relino.core.model;


import java.time.LocalDateTime;

/**
 *
 * 状态机流转如下：
 *
 *                 |----<------\
 *                |             \
 * PREPARE ---> SLEEP ---> RUNNABLE ---> FINISHED
 *    \
 *     \ -------> CANCEL
 *
 *
 * SLEEP ---> RUNNABLE {@link com.relino.core.task.ScanRunnableDelayJob}
 *
 * @author kaiqiang.he
 */
public interface Job {

    /**
     * 延迟执行 (RUNNABLE ---> SLEEP)
     *
     * @param nextExecuteTime 下一次执行时间
     */
    void delayExecute(LocalDateTime nextExecuteTime);

    /**
     * job执行结束 (RUNNABLE ---> FINISHED)
     */
    void finished();

    //void prepare();

    // void runnable();
    // see ScanRunnableDelayJob

    //void cancel();
}
