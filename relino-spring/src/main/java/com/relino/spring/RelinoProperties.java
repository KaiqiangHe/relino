package com.relino.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author kaiqiang.he
 */
@ConfigurationProperties(prefix = RelinoProperties.RELINO_AUTO_CONFIG_PREFIX)
public class RelinoProperties {

    public static final String RELINO_AUTO_CONFIG_PREFIX = "relino";

    private String appId;
    private String zkConnectStr;

    public RelinoProperties() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getZkConnectStr() {
        return zkConnectStr;
    }

    public void setZkConnectStr(String zkConnectStr) {
        this.zkConnectStr = zkConnectStr;
    }
}
