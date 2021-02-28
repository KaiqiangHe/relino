package com.relino.spring;

import com.relino.core.Relino;
import com.relino.core.config.RelinoConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @author kaiqiang.he
 */
@Configuration
@ConditionalOnBean({RelinoActionScan.class})
@EnableConfigurationProperties(RelinoProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class RelinoAutoConfiguration implements InitializingBean {

    @Resource
    private RelinoProperties properties;

    @Resource
    private DataSource dataSource;

    @Resource
    private RelinoActionScan relinoActionScan;

    public RelinoAutoConfiguration() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(properties == null || properties.getAppId() == null || properties.getZkConnectStr() == null) {
            throw new IllegalArgumentException("relino配置文件出错");
        }
    }

    @Bean
    @ConditionalOnMissingBean(Relino.class)
    public Relino relino() {
        RelinoConfig relinoConfig = new RelinoConfig(properties.getAppId(), properties.getZkConnectStr(), dataSource);
        relinoActionScan.getActionMap().forEach(relinoConfig::registerAction);
        Relino relino = new Relino(relinoConfig);
        relino.start();
        return relino;
    }
}
