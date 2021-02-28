package com.relino.spring;

import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.JobAttr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author kaiqiang.he
 */
public class ActionProxy implements Action {

    private Object target;

    private Method method;

    public ActionProxy(Object target, Method method) {

        String checkRet = checkTargetAndMethod(target, method);
        if(checkRet != null) {
            throw new IllegalArgumentException(checkRet);
        }

        this.target = target;
        this.method = method;
    }

    @Override
    public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {
        try {
            return (ActionResult) method.invoke(target, jobId, commonAttr, executeCount);
        } catch (IllegalAccessException | InvocationTargetException e) {
            // 不会执行
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查被代理对象和方法
     *
     * @return 检查通过返回null，否则返回错误信息
     */
    public static String checkTargetAndMethod(Object target, Method method) {
        if(target == null) {
            return "被代理对象不能为null";
        }
        if(method == null) {
            return "方法不能为null";
        }

        if(!Modifier.isPublic(method.getModifiers())) {
            return "方法必须为public";
        }

        // TODO: 2021/2/27 更复杂的校验

        return null;
    }
}
