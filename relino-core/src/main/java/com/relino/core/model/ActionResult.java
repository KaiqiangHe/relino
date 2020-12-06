package com.relino.core.model;

/**
 *
 *
 * @author kaiqiang.he
 */
public class ActionResult {

    /**
     * 操作是否成功
     */
    private boolean success;

    private JobAttr resultValue;

    private ActionResult() {
    }

    public static ActionResult buildSuccess(JobAttr resultValue) {
        ActionResult result = new ActionResult();
        result.success = true;
        result.resultValue = resultValue;
        return result;
    }

    public static ActionResult buildSuccess() {
        return buildSuccess(new JobAttr());
    }

    public static ActionResult buildError(JobAttr resultValue) {
        ActionResult result = new ActionResult();
        result.success = false;
        result.resultValue = resultValue;
        return result;
    }

    public static ActionResult buildError() {
        return buildError(new JobAttr());
    }

    public boolean isSuccess() {
        return success;
    }

    public JobAttr getResultValue() {
        return resultValue;
    }

}
