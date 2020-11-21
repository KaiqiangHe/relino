package com.relino.core;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author kaiqiang.he
 */
public class TestHelper {

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/relino");
        config.setUsername("root");
        config.setPassword("DQ971208");
        config.setAutoCommit(true);
        config.setConnectionTimeout(5 * 1000);  // 5s
        config.setMinimumIdle(5);
        config.setMinimumIdle(10);

        return new HikariDataSource(config);
    }

}
