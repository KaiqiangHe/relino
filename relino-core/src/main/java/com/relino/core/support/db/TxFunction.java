package com.relino.core.support.db;

/**
 * @author kaiqiang.he
 */
@FunctionalInterface
public interface TxFunction<T, R> {

    R execute(T t) throws Exception;
}
