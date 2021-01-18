package com.relino.core.support.db;

import com.relino.core.model.Job;
import com.relino.core.model.db.JobEntity;
import com.relino.core.support.JobUtils;
import com.relino.core.support.Utils;
import org.apache.commons.dbutils.handlers.MapHandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kaiqiang.he
 */
public class JobStore {

    private DBExecutor dbExecutor;

    public JobStore(DBExecutor dbExecutor) {
        Utils.checkNoNull(dbExecutor);
        this.dbExecutor = dbExecutor;
    }

    /**
     * 创建Job
     */
    private static final String INSERT_JOB_SQL =
            "insert into job(" +
                    "    job_id, idempotent_id, job_code, job_status, is_delay_job, begin_time, " +
                    "    common_attr, will_execute_time, execute_order, " +
                    "    m_action_id, m_oper_status, m_execute_count, m_retry_policy_Id, m_max_retry)" +
                    "value (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public int insertNew(Job job) throws SQLException {
        Job.Oper mOper = job.getMOper();
        Object[] params = new Object[] {
                job.getJobId(), job.getIdempotentId(), job.getJobCode(), job.getJobStatus().getCode(), job.isDelayJob(), job.getBeginTime(),
                job.getCommonAttr().asString(), job.getWillExecuteTime(), job.getExecuteOrder(),
                mOper.getAction().getBeanId(), mOper.getOperStatus().getCode(), mOper.getExecuteCount(), mOper.getRetryPolicyId(), mOper.getMaxExecuteCount()
        };

        return dbExecutor.execute(INSERT_JOB_SQL, params);
    }

    /**
     * 更新Job
     */
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
    public int updateJob(Job job, boolean updateCommonAttr) throws SQLException {
        List<Object> param = new ArrayList<>();
        String sql;
        if(updateCommonAttr) {
            sql = UPDATE_JOB_WITH_COMMON_ATTR;
            param.add(job.getCommonAttr().asString());
        } else {
            sql = UPDATE_JOB;
        }

        Job.Oper oper = job.getMOper();
        param.add(job.getJobStatus().getCode());
        param.add(job.getExecuteOrder());
        param.add(job.getWillExecuteTime());
        param.add(oper.getOperStatus().getCode());
        param.add(oper.getExecuteCount());
        param.add(job.getJobId());

        return dbExecutor.execute(sql, param.toArray());
    }

    /**
     * 根据Id查询
     * @param jobId
     * @return
     * @throws SQLException
     */
    public JobEntity queryByJobId(String jobId) throws SQLException {
        String sql = "select * from job where job_id = ?";
        Map<String, Object> row = dbExecutor.query(sql, new MapHandler(), new Object[]{jobId});
        if(row == null) {
            return null;
        }

        return JobUtils.toJobEntity(row);
    }

    /**
     * 将Job由Sleep设置为Runnable
     */

    /**
     * 将Job有Runnable设置为Sleep
     */
}
