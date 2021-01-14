package com.relino.core.support.db;

import com.relino.core.exception.RelinoException;
import com.relino.core.support.Utils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author kaiqiang.he
 */
public class DBExecutor implements TxSupport {

    private ThreadLocal<Connection> connThreadLocal = new ThreadLocal<>();

    private QueryRunner runner;
    private DataSource dataSource;

    public DBExecutor(DataSource dataSource) {
        Utils.checkNoNull(dataSource);
        this.dataSource = dataSource;
        this.runner = new QueryRunner(dataSource);
    }

    @Override
    public void beginTx() throws SQLException {
        Connection conn = getCurrentTxConnect();
        if(conn != null) {
            throw new RelinoException("不能重复调用beginTx(), 应先调用commitTx()或rollbackTx()关闭当前Connection");
        }

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            connThreadLocal.set(conn);
        } catch (Exception e) {
            closeAndRemoveConnection();
            throw e;
        }
    }

    @Override
    public void commitTx() throws SQLException {
        Connection conn = getCurrentTxConnect();
        if(conn == null) {
            throw new RelinoException("当前线程无Connection对象, 应先调用beginTx()开启事务");
        }
        try {
            conn.commit();
        } finally {
            closeAndRemoveConnection();
        }
    }

    @Override
    public void rollbackTx() throws SQLException {
        Connection conn = getCurrentTxConnect();
        if(conn == null) {
            throw new RelinoException("当前线程无Connection对象, 应先调用beginTx()开启事务");
        }
        try {
            conn.rollback();
        } finally {
            closeAndRemoveConnection();
        }
    }

    @Override
    public boolean isInTx() {
        return getCurrentTxConnect() != null;
    }

    @Override
    public Connection getCurrentTxConnect() {
        return connThreadLocal.get();
    }

    /**
     * 关闭并移除Connection对象
     */
    private void closeAndRemoveConnection() throws SQLException {
        Connection conn = getCurrentTxConnect();
        if(conn != null) {
            connThreadLocal.remove();
            if(!conn.isClosed()) {
                conn.close();
            }
        }
    }

    @Override
    public <T, R> R executeWithTx(TxFunction<T, R> func, T t) throws Exception {
        beginTx();
        try {
            R ret = func.execute(t);
            commitTx();
            return ret;
        } catch (Exception e) {
            rollbackTx();
            throw e;
        }
    }

    public int execute(String sql, Object[] params) throws SQLException {
        Connection conn = connThreadLocal.get();
        if(conn != null) {
            return runner.execute(conn, sql, params);
        } else {
            return runner.execute(sql, params);
        }
    }

    public <T> T query(String sql, ResultSetHandler<T> rsh, Object[] params) throws SQLException {
        Connection conn = connThreadLocal.get();
        if(conn != null) {
            return runner.query(conn, sql, rsh, params);
        } else {
            return runner.query(sql,rsh,  params);
        }
    }
}
