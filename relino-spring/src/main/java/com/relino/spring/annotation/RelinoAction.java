package com.relino.spring.annotation;

import java.lang.annotation.*;

/**
 * @author kaiqiang.he
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface RelinoAction {

    /**
     * // TODO: 2021/2/27 支持spring注入属性 参考 QMQ
     *
     * @return actionId
     */
    String value();

}
