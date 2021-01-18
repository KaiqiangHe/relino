package com.relino.core.model;

/**
 * @author kaiqiang.he
 */
public interface ObjectConverter<S, D> {

    /**
     * 将S转化成D，如果s为null返回null
     */
    D convert(S s);

}
