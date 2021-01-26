package com.relino.core.support;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * @author kaiqiang.he
 */
public class Utils {

    // ----------------------------------------------------------------------------
    // 常量
    public static final int TRUE = 1, FALSE = 0;

    public static final String NULL_STRING = "null", EMPTY_STRING = "";

    // ----------------------------------------------------------------------------
    // date & time
    public static final LocalDateTime NULL_DATE_TIME, MAX_DATE_TIME;
    static {
        NULL_DATE_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 1);
        MAX_DATE_TIME = LocalDateTime.of(2500, 1, 1, 0, 0,1);
    }

    private static DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String toStrDate(LocalDateTime dateTime) {
        if(dateTime == null) {
            return "null";
        }
        return dateTime.format(defaultFormatter);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if(date == null) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static <E> boolean isEmpty(Collection<E> coll) {
        return coll == null || coll.isEmpty();
    }

    public static <K, V> boolean isEmpty(Map<K, V> map) {
        return map == null || map.isEmpty();
    }

    public static <T> List<T> nullToEmptyList(List<T> list) {
        if(isEmpty(list)) {
            return new ArrayList<>();
        }

        return list;
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
    public static void checkNoNull(Object param) {
        if(param == null) {
            throw new IllegalArgumentException("参数不能为null");
        }
    }
    public static void checkNoNull(Object param, String message) {
        if(param == null) {
            throw new IllegalArgumentException(message);
        }
    }
    public static void checkNonEmpty(String str) {
        if (isEmpty(str))
            throw new NullPointerException();
    }

    // --------------------------------------------------------------------------------
    private static final int maxCacheN = 500;
    private static final Map<Integer, String> nQuestionMarkCaches = new ConcurrentHashMap<>();
    /**
     * 返回n个?, 格式如：(?, ?, ... )
     */
    public static String getNQuestionMark(int n) {
        if(n <= maxCacheN) {
            String value = nQuestionMarkCaches.get(n);
            if(value == null) {
                value = generateNQuestionMark(n);
                nQuestionMarkCaches.put(n, value);
            }
            return value;
        } else {
            return generateNQuestionMark(n);
        }
    }
    public static String generateNQuestionMark(int n) {
        if(n <= 0) {
            throw new IllegalArgumentException("参数n应大于0, n = " + n);
        }

        if(n == 1) {
            return "(?)";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("(?");
        for (int i = 0; i < n - 1; i++) {
            sb.append(",?");
        }
        sb.append(")");
        return sb.toString();
    }
}
