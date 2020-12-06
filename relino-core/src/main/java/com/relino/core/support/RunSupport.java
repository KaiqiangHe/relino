package com.relino.core.support;

/**
 * @author kaiqiang.he
 */
public interface RunSupport {

    /**
     * 执行
     */
    void execute();

    /**
     * 当前线程是否结束
     */
    boolean isTerminal();

    /**
     * 异步结束当前线程。可调用isTerminal()方法查看是否已经结束。
     */
    void terminalAsync();

    /**
     * 同步结束当前线程
     */
    void terminal();

    /**
     * 销毁
     */
    default void destroy() {}
}
