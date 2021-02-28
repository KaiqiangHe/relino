package com.relino.spring.demo.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author kaiqiang.he
 */
@Configuration
public class ActionConfig {

    /*@Bean("relino")
    public Relino relino(DataSource dataSource, RelinoActionScan relinoActionScan) {
        String appId = "hello-relino";
        String ZK_CONNECT_STR = "172.20.0.2:2181";
        RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);
        relinoActionScan.getActionMap().forEach(relinoConfig::registerAction);
        Relino relino = new Relino(relinoConfig);
        relino.start();
        return relino;
    }*/
}
