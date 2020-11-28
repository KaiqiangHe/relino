package com.relino.core.helper;

import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.JobAttr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogAction implements Action {

    public static final String logValue = "logValue";

    private static final Logger log = LoggerFactory.getLogger(LogAction.class);

    @Override
    public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {
        JobAttr retAttr = new JobAttr();
        if(executeCount < 3) {
            retAttr.setString("executeError" + executeCount, System.currentTimeMillis() + "");
            return ActionResult.buildError(retAttr);
        }
        log.error("logValue = {}", commonAttr.getString("logValue"));
        retAttr.setString("executeSuccess" + executeCount, System.currentTimeMillis() + "");
        return ActionResult.buildSuccess(retAttr);
    }
}
