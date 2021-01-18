package com.relino.core.support.bean;

import com.relino.core.support.Utils;

/**
 * @author kaiqiang.he
 */
public class BeanWrapper<T> {

    private String beanId;

    private T bean;

    public BeanWrapper(String beanId, T bean) {
        Utils.checkNonEmpty(beanId);
        Utils.checkNoNull(bean);
        this.beanId = beanId;
        this.bean = bean;
    }

    public String getBeanId() {
        return beanId;
    }

    public T getBean() {
        return bean;
    }
}
