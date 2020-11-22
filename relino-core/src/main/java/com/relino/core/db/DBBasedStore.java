package com.relino.core.db;

import com.relino.core.model.Job;
import com.relino.core.model.JobEntity;
import com.relino.core.model.Oper;
import com.relino.core.support.Utils;
import org.apache.commons.dbutils.QueryRunner;
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
public class DBBasedStore implements Store {

    private DataSource dataSource;

    private QueryRunner runner;

    public DBBasedStore(DataSource dataSource) {
        this.dataSource = dataSource;
        runner = new QueryRunner(dataSource);
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
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

        return runner.execute(
                INSERT_JOB_SQL,
                job.getJobId(), job.getIdempotentId(), job.getJobCode(), job.getJobStatus().getCode(), job.isDelayJob(), job.getBeginTime(),
                job.getCommonAttr().asString(), job.getWillExecuteTime(), job.getExecuteOrder(),
                mOper.getActionId(), mOper.getOperStatus().getCode(), mOper.getExecuteCount(), mOper.getRetryPolicyId(), mOper.getMaxExecuteCount()
                );
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

        return runner.execute(sql, param.toArray());
    }

    // -----------------------------------------------------------------------------------------------------------------
    @Override
    public String kvSelectForUpdate(Connection conn, String key) throws SQLException {
        Map<String, Object> row = runner.query(conn, "select v from kv where k = ? for update", new MapHandler(), key);
        if(Utils.isEmpty(row)) {
            return null;
        }
        Object v = row.get("v");
        return v == null ? null : (String) v;
    }

    @Override
    public int kvUpdateValue(Connection conn, String key, String newValue) throws SQLException {
        return runner.execute(conn, "update kv set v = ? where k = ?", newValue, key);
    }

    @Override
    public List<JobEntity> selectJobEntity(long lastExecuteJobId, int limit) throws SQLException {
        List<Map<String, Object>> rows = runner.query(
                "select * from job where job_status = 2 and execute_order > ? order by execute_order limit ?",
                new MapListHandler(), lastExecuteJobId, limit);

        if(Utils.isEmpty(rows)) {
            return Collections.emptyList();
        }

        return rows.stream().map(r -> {
            // TODO: 2020/11/22
            JobEntity entity = new JobEntity();
            entity.setId(((BigInteger) r.get("id")).longValue());
            entity.setJobId((String) r.get("job_id"));
            entity.setExecuteOrder(((long) r.get("execute_order")));
            return entity;
        }).collect(Collectors.toList());
    }
    // -----------------------------------------------------------------------------------------------------------------



    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) {
        return null;
    }

    @Override
    public void setDelayJobRunnable(List<IdAndExecuteOrder> updateData) {

    }
}
