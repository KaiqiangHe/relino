package com.relino.core.db;

import com.relino.core.exception.RelinoException;
import com.relino.core.model.Job;
import com.relino.core.model.JobEntity;
import com.relino.core.model.Oper;
import com.relino.core.support.Utils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author kaiqiang.he
 */
public class DBBasedStore extends Store {

    private ThreadLocal<Connection> connThreadLocal = new ThreadLocal<>();

    private QueryRunner runner;

    public DBBasedStore(DataSource dataSource) {
        super(dataSource);
        runner = new QueryRunner(dataSource);
    }

    @Override
    public void beginTx() throws SQLException {
        Connection conn = connThreadLocal.get();
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
        Connection conn = connThreadLocal.get();
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
        Connection conn = connThreadLocal.get();
        if(conn == null) {
            throw new RelinoException("当前线程无Connection对象, 应先调用beginTx()开启事务");
        }
        try {
            conn.rollback();
        } finally {
            closeAndRemoveConnection();
        }
    }

    /**
     * 关闭并移除Connection对象
     */
    private void closeAndRemoveConnection() throws SQLException {
        Connection conn = connThreadLocal.get();
        if(conn != null) {
            connThreadLocal.remove();
            if(!conn.isClosed()) {
                conn.close();
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    private static final String INSERT_JOB_SQL =
            "insert into job(" +
                    "    job_id, idempotent_id, job_code, job_status, is_delay_job, begin_time, " +
                    "    common_attr, will_execute_time, execute_order, " +
                    "    m_action_id, m_oper_status, m_execute_count, m_retry_policy_Id, m_max_retry)" +
                    "value (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    @Override
    public int insertJob(Job job) throws SQLException {
        Oper mOper = job.getMOper();
        Object[] params = new Object[] {
                job.getJobId(), job.getIdempotentId(), job.getJobCode(), job.getJobStatus().getCode(), job.isDelayJob(), job.getBeginTime(),
                job.getCommonAttr().asString(), job.getWillExecuteTime(), job.getExecuteOrder(),
                mOper.getActionId(), mOper.getOperStatus().getCode(), mOper.getExecuteCount(), mOper.getRetryPolicyId(), mOper.getMaxExecuteCount()
        };

        return execute(INSERT_JOB_SQL, params);
    }

    @Override
    public JobEntity queryByJobId(String jobId) throws SQLException {
        String sql = "select * from job where job_id = ?";
        Map<String, Object> row = query(sql, new MapHandler(), new Object[]{jobId});
        if(row == null) {
            return null;
        }

        return toJobEntity(row);
    }

    // -----------------------------------------------------------------------------------------------------------------
    public static final String UPDATE_JOB =
            "update job " +
                    "set job_status = ?, execute_order = ?, will_execute_time = ?, " +
                    "    m_oper_status = ?, m_execute_count = ? " +
                    "where job_id = ?";
    public static final String UPDATE_JOB_WITH_COMMON_ATTR =
            "update job " +
                    "set common_attr = ?, " +
                    "    job_status = ?, execute_order = ?, will_execute_time = ?, " +
                    "    m_oper_status = ?, m_execute_count = ? " +
                    "where job_id = ?";
    @Override
    public int updateJob(Job job, boolean updateCommonAttr) throws SQLException {
        List<Object> param = new ArrayList<>();
        String sql;
        if(updateCommonAttr) {
            sql = UPDATE_JOB_WITH_COMMON_ATTR;
            param.add(job.getCommonAttr().asString());
        } else {
            sql = UPDATE_JOB;
        }

        Oper oper = job.getMOper();
        param.add(job.getJobStatus().getCode());
        param.add(job.getExecuteOrder());
        param.add(job.getWillExecuteTime());
        param.add(oper.getOperStatus().getCode());
        param.add(oper.getExecuteCount());
        param.add(job.getJobId());

        return execute(sql, param.toArray());
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public String kvSelectForUpdate(String key) throws SQLException {
        String sql = "select v from kv where k = ? for update";
        Map<String, Object> row = query(sql, new MapHandler(), new Object[]{key});

        if(Utils.isEmpty(row)) {
            return null;
        }
        Object v = row.get("v");
        return v == null ? null : (String) v;
    }

    @Override
    public int kvUpdateValue(String key, String newValue) throws SQLException {
        return execute("update kv set v = ? where k = ?", new Object[] {newValue, key});
    }

    @Override
    public List<JobEntity> selectJobEntity(long lastExecuteJobId, int limit) throws SQLException {

        String sql = "select * from job where job_status = 2 and execute_order > ? order by execute_order limit ?";
        List<Map<String, Object>> rows = query(sql, new MapListHandler(), new Object[]{lastExecuteJobId, limit});
        if(Utils.isEmpty(rows)) {
            return Collections.emptyList();
        }

        return rows.stream().map(this::toJobEntity).collect(Collectors.toList());
    }

    // TODO: 2020/11/22
    private JobEntity toJobEntity(Map<String, Object> row) {
        if(row == null) {
            return null;
        }

        JobEntity entity = new JobEntity();
        entity.setId(((BigInteger) row.get("id")).longValue());
        entity.setJobId((String) row.get("job_id"));
        entity.setExecuteOrder(((long) row.get("execute_order")));
        return entity;
    }
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) {
        return null;
    }

    @Override
    public void setDelayJobRunnable(List<IdAndExecuteOrder> updateData) {

    }

    // -----------------------------------------------------------------------------------------------------------------
    //
    private int execute(String sql, Object[] params) throws SQLException {
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
