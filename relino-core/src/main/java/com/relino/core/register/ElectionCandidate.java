package com.relino.core.register;

/**
 * zk Master节点选取
 *
 * @author kaiqiang.he
 */
public interface ElectionCandidate {
    
    /**
     * 当被选举为Master节点时执行该方法
     *
     * 注意：
     * 1. 不要吞掉任何InterruptedException异常
     * 2. 如果Thread.interrupted() == true 主动结束
     */
    void executeWhenCandidate() throws InterruptedException;

    void destroy();
}
