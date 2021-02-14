package com.relino.demo.action;

import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.JobAttr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class SayHello implements Action {

    private static final Logger log = LoggerFactory.getLogger(SayHello.class);

    // 实现Action接口，创建SayHello类
    @Override
    public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

        try {

            String userId = commonAttr.getString("userId"); // 获取Job属性
            Thread.sleep(100);
            log.info("Hello {}", userId);
            return ActionResult.buildSuccess();

        } catch (Exception e) {
            log.error("sendSms error, jobId = {}", jobId, e);
            JobAttr retAttr = new JobAttr();
            retAttr.setLocalDateTime("errorTime" + executeCount, LocalDateTime.now());
            retAttr.setString("errorException" + executeCount, e.getMessage());
            return ActionResult.buildError(retAttr);
        }
    }

}
