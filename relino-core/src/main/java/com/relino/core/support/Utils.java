package com.relino.core.support;

import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * @author kaiqiang.he
 */
public class Utils {

    // ----------------------------------------------------------------------------
    // 常量
    public static final int TRUE = 1, FALSE = 0;

    public static final String NULL_STRING = "null", EMPTY_STRING = "";

    public static final LocalDateTime NULL_DATE_TIME, MAX_DATE_TIME;
    static {
        NULL_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 1);
        MAX_DATE_TIME = LocalDateTime.of(2500, 1, 1, 0, 0,1);
    }


    // ------------------------------------------------------------------------------
    // 参数校验
    public static <T> void check(T param, Predicate<T> predicate, String message) {
        if(predicate.test(param)) {
            throw new IllegalArgumentException(message);
        }
    }
    public static <T> void check(T param, Predicate<T> predicate) {
        if(predicate.test(param)) {
            throw new IllegalArgumentException();
        }
    }
    public static void checkNonEmpty(String str) {
        if (str == null)
            throw new NullPointerException();
    }
}