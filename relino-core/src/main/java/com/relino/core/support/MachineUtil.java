package com.relino.core.support;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public class MachineUtil {

    public static final String LOCAL_IP;
    public static final int LOCAL_IP_NUM;
    static {
        LOCAL_IP = getIPStr();
        LOCAL_IP_NUM = parseIpNum(LOCAL_IP);
    }
    public static String getIPStr() {
        List<String> ipStrList = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface intf = enumeration.nextElement();
                if (intf.isLoopback() || intf.isVirtual()) {    // 排除回文地址、虚拟地址
                    continue;
                }
                Enumeration<InetAddress> inets = intf.getInetAddresses();
                while (inets.hasMoreElements()) {
                    InetAddress addr = inets.nextElement();
                    if (addr.isLoopbackAddress() || !addr.isSiteLocalAddress() || addr.isAnyLocalAddress()) {
                        continue;
                    }
                    ipStrList.add(addr.getHostAddress());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("获取本地ip失败", e);
        }

        if(ipStrList.isEmpty()) {
            throw new RuntimeException("获取本地ip失败, ipStrList为空");
        }
        return ipStrList.get(0);
    }

    public static int parseIpNum(String ipStr) {
        // 取 ip 的各段
        String[] ipSlices = ipStr.split("\\.");
        int rs = 0;
        for (int i = 0; i < ipSlices.length; i++) {
            // 将 ip 的每一段解析为 int，并根据位置左移 8 位
            int intSlice = Integer.parseInt(ipSlices[i]) << 8 * i;
            // 或运算
            rs = rs | intSlice;
        }
        return rs;
    }

    // -----------------------------------------------------------------------------------
    public static final int PID;
    static {
        PID = getPid();
    }
    public static int getPid() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName(); // format: "pid@hostname"
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Throwable e) {
            throw new RuntimeException("获取pid失败", e);
        }
    }

}
