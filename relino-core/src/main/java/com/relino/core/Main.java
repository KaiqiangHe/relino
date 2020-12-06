package com.relino.core;

import com.relino.core.db.DBBasedStore;
import com.relino.core.db.Store;
import com.relino.core.model.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author kaiqiang.he
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        // create datasource
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/relino?useSSL=false&useSSL=false&serverTimezone=Asia/Shanghai");
        config.setUsername("root");
        config.setPassword("DQ971208");
        config.setAutoCommit(true);
        config.setConnectionTimeout(5 * 1000);  // 5s
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        DataSource dataSource = new HikariDataSource(config);

        // create action
        String sendSmsActionId = "sendSms";
        ActionManager.register(sendSmsActionId, new SendSms());

        Store store = new DBBasedStore(dataSource);
        Job.setStore(store);
        Relino relino = new Relino(store, 100, 100, 5);

        // 每秒创建10个延迟job测试
        while (true) {
            try {
                Oper mOper = Oper.builder(sendSmsActionId).maxExecuteCount(3).build();
                JobAttr initAttr = new JobAttr();
                initAttr.setString("userId", "orange" + System.currentTimeMillis());
                initAttr.setString("sendData", "Hello, Test Relino.");
                Job job = relino.jobProducer.builder(mOper).commonAttr(initAttr).delayJob(10 + ThreadLocalRandom.current().nextInt(100)).build();
                relino.jobProducer.createJob(job);
                log.info("crate job success, jobId = {}", job.getJobId());
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("crate job error ", e);
            }
        }
    }

    static class SendSms implements Action {

        @Override
        public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

            try {
                String userId = commonAttr.getString("userId");
                String sendData = commonAttr.getString("sendData");

                // 模拟执行异常
                if(ThreadLocalRandom.current().nextInt(100) == 0) {
                    throw new RuntimeException("mock exception, jobId = " + jobId);
                }

                Thread.sleep(100);
                log.info("sendSms success, userId = {}, sendData = {}", userId, sendData);
                return ActionResult.buildSuccess();
            } catch (Exception e) {
                log.error("sendSms error, jobId = {}", jobId, e);
                JobAttr retAttr = new JobAttr();
                retAttr.setLocalDateTime("errorTime" + executeCount, LocalDateTime.now());
                retAttr.setString("errorException" + executeCount, e.getMessage());
                return ActionResult.buildError(retAttr);
            }
        }
    }

}
