package com.relino.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kaiqiang.he
 */
public enum OperStatus {

    /**
     * INIT -> RUNNABLE -> SUCCESS_FINISHED | FAILED_FINISHED
     */
    INIT(101, "初始化"),
    RUNNABLE(102, "可执行"),
    SUCCESS_FINISHED(103, "完成"),
    FAILED_FINISHED(104, "完成");

    private int code;
    private String desc;

    private static final Map<Integer, OperStatus> enumMap;
    static {
        enumMap = new HashMap<>();
        for (OperStatus value : OperStatus.values()) {
            enumMap.put(value.getCode(), value);
        }
    }
    /**
     * @return nullable
     */
    public static OperStatus toEnum(int code) {
        return enumMap.get(code);
    }


    OperStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
