package com.relino.core.db;

import com.relino.core.model.Job;
import com.relino.core.model.Oper;
import org.apache.commons.dbutils.QueryRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) {
        return null;
    }

    @Override
    public void setDelayJobRunnable(List<IdAndExecuteOrder> updateData) {

    }
}
