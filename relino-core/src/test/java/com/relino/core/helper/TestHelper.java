package com.relino.core.helper;

import com.relino.core.model.Job;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.UUIDIdGenerator;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * @author kaiqiang.he
 */
public class TestHelper {

    public static final String LOG_ACTION_ID = "logAction";
    public static final String SleepAndLogAction_ID = "SleepAndLogAction";
    public static final String ZK_CONNECT_STR = "127.0.0.1:2181";

    public static void testBootStrap() {

        /*ActionManager.register(LOG_ACTION_ID, new LogAction());
        ActionManager.register(SleepAndLogAction_ID, new SleepAndLogAction());*/
    }

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/relino?useSSL=false&useSSL=false&serverTimezone=Asia/Shanghai");
        config.setUsername("root");
        config.setPassword("DQ971208");
        config.setAutoCommit(true);
        config.setConnectionTimeout(5 * 1000);  // 5s
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);

        return new HikariDataSource(config);
    }

    public static DBExecutor getDBExecutor() {
        return new DBExecutor(getDataSource());
    }

    public static Job getJob(IdGenerator idGenerator, String actionId) {
        /*JobAttr commonAttr = new JobAttr();
        commonAttr.setString(LogAction.logValue, "hello-" + System.currentTimeMillis());
        Job job = new JobBuilder(idGenerator.getNext(), actionId)
                .maxExecuteCount(10)
                .retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY)
                .delayExecute(100).commonAttr(commonAttr).build();

        return job;*/
        return null;
    }

    public static IdGenerator getIdGenerator() {
        return new UUIDIdGenerator();
    }

}
