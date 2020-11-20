package com.relino.core.model;

import com.relino.core.support.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * 支持的数据类型: String long double boolean LocalDateTime
 *
 * @author kaiqiang.he
 */
public class JobAttr {

    private Map<String, String> attr = new HashMap<>();

    public JobAttr() {
    }

    public String asString() {
        // TODO: 2020/11/20
        return "";
    }
    public static JobAttr asObj(String data) {
        return new JobAttr();
        // TODO: 2020/11/20
    }

    public void addAll(JobAttr newAttr) {
        this.attr.putAll(newAttr.attr);
    }

    public boolean isEmpty() {
        return attr.isEmpty();
    }

    public void setString(String key, String value) {
        setAttr(key, value, (v) -> v);
    }

    public String getString(String key) {
        return getAttr(key, (s) -> s);
    }

    public void setLong(String key, long value) {
        setAttr(key, value, (v) -> Long.toString(v));
    }

    public Long getLong(String key) {
        return getAttr(key, Long::parseLong);
    }

    public void setDouble(String key, double value) {
        setAttr(key, value, v -> Double.toString(v));
    }

    public Double getDouble(String key) {
        return getAttr(key, Double::parseDouble);
    }

    public void setBoolean(String key, boolean value) {
        setAttr(key, value, n -> Boolean.toString(value));
    }

    public Boolean getBoolean(String key) {
        return getAttr(key, Boolean::parseBoolean);
    }

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public void setLocalDateTime(String key, LocalDateTime value) {
        setAttr(key, value, n -> value.format(DEFAULT_FORMATTER));
    }
    public LocalDateTime getLocalDateTime(String key) {
        return getAttr(key, v -> LocalDateTime.parse(v, DEFAULT_FORMATTER));
    }

    // -----------------------------------------------------------------------------------
    //
    /**
     *
     * @param key not empty
     * @param value not null
     */
    private <T> void setAttr(String key, T value, Function<T, String> toStringFunc) {
        Utils.checkNonEmpty(key);
        Objects.requireNonNull(value);
        attr.put(key, toStringFunc.apply(value));
    }

    /**
     * @return nullable
     */
    private <T> T getAttr(String key, Function<String, T> toValueFunc) {
        String str = attr.get(key);
        if(str == null) {
            return null;
        }

        return toValueFunc.apply(str);
    }

}
