package com.relino.core.support.bean;

import com.relino.core.exception.RelinoException;
import com.relino.core.support.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author kaiqiang.he
 */
public class BeanManager<T> {

    private Map<String, BeanWrapper<T>> beanHolders = new HashMap<>();

    public BeanManager() { }

    /**
     * 注册一个bean
     *
     * @param beanId not empty
     * @param bean not null
     */
    public void register(String beanId, T bean) {
        Utils.checkNonEmpty(beanId);
        Objects.requireNonNull(bean);

        if(containsAction(beanId)) {
            throw new RelinoException("beanId = " + beanId + "已经存在");
        }

        beanHolders.put(beanId, new BeanWrapper<>(beanId, bean));
    }

    public boolean containsAction(String beanId) {
        return beanHolders.containsKey(beanId);
    }

    /**
     * @return nullable
     */
    public T getBean(String beanId) {
        BeanWrapper<T> beanWrapper = getBeanWrapper(beanId);
        if(beanWrapper != null) {
            return beanWrapper.getBean();
        }
        return null;
    }

    /**
     * @return nullable
     */
    public BeanWrapper<T> getBeanWrapper(String beanId) {
        return beanHolders.get(beanId);
    }

}
