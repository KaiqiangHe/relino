package com.relino.core.register;

/**
 * zk 节点选取
 *
 * @author kaiqiang.he
 */
public interface ElectionCandidate {
    
    /**
     * 当被选举为主节点是调用该方法执行
     */
    void executeWhenCandidate() throws Exception;
    
    /**
     * 停止执行
     */
    void stopExecute();
}
