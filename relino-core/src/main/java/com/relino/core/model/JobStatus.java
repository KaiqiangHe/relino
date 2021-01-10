package com.relino.core.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author kaiqiang.he
 */
public enum JobStatus {

    PREPARE(0, "准备"),
    SLEEP(1, "等待"),
    RUNNABLE(2, "可执行"),
    FINISHED(3, "已完成"),
    CANCEL(4, "取消");

    private int code;
    private String desc;

    JobStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static final Map<Integer, JobStatus> enumMap;
    static {
        enumMap = new HashMap<>();
        for (JobStatus value : JobStatus.values()) {
            enumMap.put(value.getCode(), value);
        }
    }
    /**
     * @return nullable
     */
    public static JobStatus toEnum(int code) {
        return enumMap.get(code);
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
