package com.relino.core.db;

import com.relino.core.exception.RelinoException;
import com.relino.core.model.Job;
import com.relino.core.model.db.ExecuteTimeEntity;
import com.relino.core.model.db.JobEntity;
import com.relino.core.model.JobStatus;
import com.relino.core.model.Oper;
import com.relino.core.support.Utils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import javax.sql.DataSource;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
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

    private JobEntity toJobEntity(Map<String, Object> row) {
        if(row == null) {
            return null;
        }

        JobEntity entity = new JobEntity();
        entity.setId(((BigInteger) row.get("id")).longValue());
        entity.setJobId((String) row.get("job_id"));
        entity.setIdempotentId((String) row.get("idempotent_id"));
        entity.setJobCode((String) row.get("job_code"));

        entity.setIsDelayJob((Integer) row.get("is_delay_job"));
        entity.setBeginTime(Utils.toLocalDateTime((Date) row.get("begin_time")));

        entity.setCommonAttr((String) row.get("common_attr"));

        entity.setWillExecuteTime(Utils.toLocalDateTime((Date) row.get("will_execute_time")));
        entity.setJobStatus((Integer) row.get("job_status"));
        entity.setExecuteOrder(((long) row.get("execute_order")));

        entity.setMActionId((String) row.get("m_action_id"));
        entity.setMOperStatus((Integer) row.get("m_oper_status"));
        entity.setMExecuteCount((Integer) row.get("m_execute_count"));
        entity.setMRetryPolicyId((String) row.get("m_retry_policy_id"));
        entity.setMMaxRetry((Integer) row.get("m_max_retry"));

        entity.setCreateTime(Utils.toLocalDateTime((Date) row.get("create_time")));

        return entity;
    }
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) throws SQLException {
        String sql = "select id from job where job_status = 1 and will_execute_time >= ? and will_execute_time <= ? order by will_execute_time limit ?";
        Object[] params = new Object[]{Utils.toStrDate(start), Utils.toStrDate(end), limit};
        List<BigInteger> ids = query(sql, new ColumnListHandler<>("id"), params);
        if(Utils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return ids.stream().map(BigInteger::longValue).collect(Collectors.toList());
        }
    }

    @Override
    public void setDelayJobRunnable(List<IdAndExecuteOrder> elems) throws SQLException {

        if(elems == null || elems.isEmpty()) {
            return ;
        }

        List<Object> params = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("update job set job_status = ?, execute_order = ");
        params.add(JobStatus.RUNNABLE.getCode());
        sb.append("case id ");
        elems.forEach(v -> {
            sb.append("when ? then ? ");
            params.add(v.getId());
            params.add(v.getExecuteOrder());
        });
        sb.append("end ");
        sb.append("where id in ").append(getNQuestionMark(elems.size()));
        List<Long> ids = elems.stream().map(IdAndExecuteOrder::getId).collect(Collectors.toList());
        params.addAll(ids);

        execute(sb.toString(), params.toArray());
    }

    @Override
    public void insertExecuteRecord(long executeOrder, LocalDateTime time) throws SQLException {
        String sql = "insert into execute_time(execute_order, execute_job_time) value (?, ?)";
        Object[] params = new Object[]{executeOrder, Utils.toStrDate(time)};

        execute(sql, params);
    }

    @Override
    public ExecuteTimeEntity selectDeadJobByTime(LocalDateTime time) throws SQLException {
        String sql = "select id, execute_order, execute_job_time from execute_time where execute_job_time <= ? order by id limit 1";
        Map<String, Object> row = query(sql, new MapHandler(), new Object[]{Utils.toStrDate(time)});
        if(row == null) {
            return null;
        }

        ExecuteTimeEntity ret = new ExecuteTimeEntity();
        ret.setId(((BigInteger) row.get("id")).longValue());
        ret.setExecuteOrder(((long) row.get("execute_order")));
        ret.setExecuteJobTime(Utils.toLocalDateTime((Date) row.get("execute_job_time")));
        return ret;
    }

    @Override
    public int deleteExecuteTimeRecord(long id) throws SQLException {
        String sql = "delete from execute_time where id = ?";
        return execute(sql, new Object[]{id});
    }

    @Override
    public List<Long> getDeadJobs(long startExecuteOrder, long endExecuteOrder) throws SQLException {
        String sql = "select id from job where job_status = 2 and execute_order > ? and execute_order <= ?";
        List<BigInteger> ids = query(sql, new ColumnListHandler<>("id"), new Object[]{startExecuteOrder, endExecuteOrder});
        if(Utils.isEmpty(ids)) {
            return Collections.emptyList();
        } else {
            return ids.stream().map(BigInteger::longValue).collect(Collectors.toList());
        }
    }

    @Override
    public int updateDeadJobs(List<Long> ids, LocalDateTime willExecuteTime) throws SQLException {
        if(Utils.isEmpty(ids)) {
            return 0;
        }

        int idSize = ids.size();
        String sql = "update job set job_status = 1, will_execute_time = ? where id in " + getNQuestionMark(idSize);
        Object[] param = new Object[idSize + 1];
        param[0] = Utils.toStrDate(willExecuteTime);
        for (int i = 0; i < idSize; i++) {
            param[i + 1] = ids.get(i);
        }
        return execute(sql, param);
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

    /**
     * 返回n个?, 格式如：(?, ?, ... )
     *
     * // TODO: 2020/11/1  优化, 缓存
     * @return
     */
    private String getNQuestionMark(int n) {
        if(n <= 0) {
            throw new IllegalArgumentException("参数n应大于0, n = " + n);
        }

        if(n == 1) {
            return "(?)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(?");
        for (int i = 0; i < n - 1; i++) {
            sb.append(",?");
        }
        sb.append(")");
        return sb.toString();
    }
}
