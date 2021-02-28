package com.relino.spring;

import com.relino.core.model.Action;
import com.relino.spring.annotation.RelinoAction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 扫描使用@RelinoAction注解的方法，并生成相应的代理Action对象，
 * 可通过{@link RelinoActionScan#getActionMap()}获取
 *
 * @author kaiqiang.he
 */
public class RelinoActionScan implements BeanPostProcessor {

    private Map<String, Action> actionMap = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        registerAction(bean);
        return bean;
    }

    private void registerAction(Object bean) {

        Class<?> clazz = bean.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {

            RelinoAction annotation = AnnotationUtils.findAnnotation(method, RelinoAction.class);
            if(annotation == null) {
                continue;
            }

            String methodName = clazz.getName() + "#" + method.getName();

            // 获取actionId
            String actionId = annotation.value();
            if(actionId.length() == 0) {
                throw new IllegalArgumentException("@RelinoAction注解value参数不能为空, method = " + methodName);
            }

            // 校验Method
            String checkRet = ActionProxy.checkTargetAndMethod(bean, method);
            if(checkRet != null) {
                throw new IllegalStateException("@RelinoAction注解的方法错误, 原因：" + checkRet + ", method = " + methodName);
            }

            if(actionMap.containsKey(actionId)) {
                throw new IllegalArgumentException("重复注册Relino action, actionId = " + actionId + ", method = " + methodName);
            }
            actionMap.put(actionId, new ActionProxy(bean, method));
        }
    }

    public Map<String, Action> getActionMap() {
        return actionMap;
    }
}
