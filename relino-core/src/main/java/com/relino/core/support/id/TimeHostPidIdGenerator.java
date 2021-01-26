package com.relino.core.support.id;

import com.relino.core.support.MachineUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于时间、ip地址、进程pid的IdGenerator
 *
 * @author kaiqiang.he
 */
public class TimeHostPidIdGenerator implements IdGenerator {

    private AtomicLong count = new AtomicLong(1);

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");

    @Override
    public String getNext() {

        String timeStr = LocalDateTime.now().format(TIME_FORMATTER).substring(2);

        StringBuilder sb = new StringBuilder();
        sb.append(timeStr).append(".");
        sb.append(MachineUtil.LOCAL_IP_NUM).append(".");
        sb.append(MachineUtil.PID).append(".");
        sb.append(count.getAndIncrement());

        return sb.toString();
    }
}
