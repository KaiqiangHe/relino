package com.kaiqiang.relino.core.ops;

public interface Execute {

    /**
     * // TODO: 2020/10/11 需补充很多参数
     *
     * @param executeCount 已经执行的次数, 第一次执行时值为0
     */
    Result execute(int executeCount);

}
