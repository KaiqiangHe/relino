package com.relino.demo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author kaiqiang.he
 */
public class RelinoDemoHelper {

    public static final Config demoConfig;
    static {
        demoConfig = new Config();
        try {
            Properties properties = new Properties();
            InputStream in = RelinoDemoHelper.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);
            demoConfig.zkConnectStr = properties.getProperty("DEFAULT_ZK_CONNECT_STR");
            demoConfig.jdbcUrl = properties.getProperty("DEFAULT_JDBC_URL");
            demoConfig.dbUser = properties.getProperty("DEFAULT_DB_USER");
            demoConfig.dbPassword = properties.getProperty("DEFAULT_DB_PASSWORD");

        } catch (IOException e) {
            throw new RuntimeException("读取config.properties配置文件失败", e);
        }
    }

    public static DataSource newDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(demoConfig.getJdbcUrl());
        config.setUsername(demoConfig.getDbUser());
        config.setPassword(demoConfig.getDbPassword());
        config.setAutoCommit(true);
        config.setConnectionTimeout(5 * 1000);  // 5s
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        return new HikariDataSource(config);
    }

    public static class Config {
        private String zkConnectStr;
        private String jdbcUrl;
        private String dbUser;
        private String dbPassword;

        public String getZkConnectStr() {
            return zkConnectStr;
        }

        public String getJdbcUrl() {
            return jdbcUrl;
        }

        public String getDbUser() {
            return dbUser;
        }

        public String getDbPassword() {
            return dbPassword;
        }
    }
}
