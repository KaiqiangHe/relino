package com.relino.spring.demo.action;

import com.relino.core.model.ActionResult;
import com.relino.core.model.JobAttr;
import com.relino.spring.annotation.RelinoAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
@Service
public class Actions {

    private static final Logger log = LoggerFactory.getLogger(Actions.class);

    @RelinoAction("sayHello")
    public ActionResult sayHelloAction(String jobId, JobAttr commonAttr, int executeCount) {

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
