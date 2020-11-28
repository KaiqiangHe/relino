package com.relino.core.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * 1. 通过共享变量的方式结束
 *
 * 重写execute0()方法, 通过wannaStop()判断是否结束程序, 例如：
 * if(wannaStop()) {
 *   // 结束线程
 * }
 *
 * @author kaiqiang.he
 */
public abstract class AbstractRunSupport implements RunSupport {

    private static final Logger log = LoggerFactory.getLogger(AbstractRunSupport.class);

    private volatile Status status = Status.NOT_STOP;

    private final Object statusChangeLock = new Object();

    /**
     * NOT_STOP -> STOP_PRE -> STOP
     */
    enum Status {
        NOT_STOP, STOP_PRE, STOP
    }

    /**
     * 获取当前的状态
     */
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean isTerminal() {
        return status ==  Status.STOP;
    }

    @Override
    public void terminalAsync() {
        synchronized (statusChangeLock) {
            if(status == Status.NOT_STOP) {
                status = Status.STOP_PRE;
            }
        }
    }

    @Override
    public void terminal() {
        synchronized (statusChangeLock) {
            try {
                if(status == Status.NOT_STOP) {
                    status = Status.STOP_PRE;
                    statusChangeLock.wait();
                } else if(status == Status.STOP_PRE) {
                    statusChangeLock.wait();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void execute() {
        try {
            execute0();
        } catch (Exception e) {
            log.error("执行异常 ", e);
        } finally {
            synchronized (statusChangeLock) {
                status = Status.STOP;
                statusChangeLock.notifyAll();
            }
        }
    }

    protected boolean wannaStop() {
        return status == Status.STOP_PRE;
    }

    protected abstract void execute0() throws SQLException;
}
