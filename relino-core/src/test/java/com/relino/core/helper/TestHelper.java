package com.relino.core.helper;

import com.relino.core.JobProducer.JobBuilder;
import com.relino.core.db.DBBasedStore;
import com.relino.core.db.Store;
import com.relino.core.model.ActionManager;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.Oper;
import com.relino.core.model.retry.IRetryPolicyManager;
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

    public static void testBootStrap() {

        ActionManager.register(LOG_ACTION_ID, new LogAction());
        ActionManager.register(SleepAndLogAction_ID, new SleepAndLogAction());
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

    public static Store getStore() {
        return new DBBasedStore(getDataSource());
    }

    public static Job getJob(IdGenerator idGenerator, String actionId) {
        Oper oper = Oper.builder(actionId).maxExecuteCount(10).retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY).build();
        JobAttr commonAttr = new JobAttr();
        commonAttr.setString(LogAction.logValue, "hello-" + System.currentTimeMillis());
        Job job = new JobBuilder(idGenerator.getNext(), oper).delayJob(100).commonAttr(commonAttr).build();

        return job;
    }

    public static IdGenerator getIdGenerator() {
        return new UUIDIdGenerator();
    }

}
