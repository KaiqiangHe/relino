package com.relino.core.helper;

import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.JobAttr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

public class SleepAndLogAction implements Action {

    private static final Logger log = LoggerFactory.getLogger(SleepAndLogAction.class);

    @Override
    public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

        JobAttr resultValue = new JobAttr();
        if(ThreadLocalRandom.current().nextInt(3) == 0) {
            resultValue.setLocalDateTime("executeErrorTime" + executeCount, LocalDateTime.now());
            return ActionResult.buildError(resultValue);
        } else {
            Long sleepTime = commonAttr.getLong("sleepTime");
            if(sleepTime == null) {
                sleepTime = 1000L;
            }

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            log.info("{} execute success. time = {}", jobId, LocalDateTime.now());
            resultValue.setLocalDateTime("executeSuccessTime", LocalDateTime.now());
            return ActionResult.buildSuccess(resultValue);
        }
    }
}
