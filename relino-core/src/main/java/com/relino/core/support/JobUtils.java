package com.relino.core.support;

import com.relino.core.model.db.JobEntity;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

/**
 * @author kaiqiang.he
 */
public class JobUtils {

    public static JobEntity toJobEntity(Map<String, Object> row) {
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

}
