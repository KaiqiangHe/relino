package com.relino.core.support.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author kaiqiang.he
 */
public interface TxSupport {

    /**
     * 开启事务
     */
    void beginTx() throws SQLException;

    /**
     * 提交事务
     */
    void commitTx() throws SQLException;

    /**
     * 回滚事务
     */
    void rollbackTx() throws SQLException;

    /**
     * 是否在事务中
     */
    boolean isInTx();

    /**
     * 获取当前事务中的Connection对象
     *
     * @return nullable 未在事物中调用该方法时返回null
     */
    Connection getCurrentTxConnect();

    /**
     * 将func在事务中执行，如果func执行中有异常抛出，则回滚事务，否则提交事务
     */
    <T, R> R executeWithTx(TxFunction<T, R> func, T t) throws Exception;
}
