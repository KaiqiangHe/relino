package com.relino.core;

import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.retry.IRetryPolicyManager;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * RoadMap:
 * 1. 考虑系统整体设计，细化Job模型职责，完善job execute方法，完善用户使用API，可以用UML类图的形式描述
 *    -> 基本完成
 * 2. 重新设计Store层，以事务、悲观锁、业务相关sql等分开。不应在该层直接映射到sql，应该做业务层的抽象 完成
 *    -> 后续可参考mybatis SqlSession设计 目前的方案足够了
 *
 * 3. 完善各种配置 完成
 * 4. 参考其他系统启动，完善项目启动流程
 * 5. 更充分的测试 & 自动化测试
 * V1.2
 *
 * 参考dubbo增加filter listener机制
 * 全局事件监听
 * 接入Spring
 * 等等
 * V1.3
 *
 * @author kaiqiang.he
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        String ZK_CONNECT_STR = "127.0.0.1:2181";

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

        RelinoConfig relinoConfig = new RelinoConfig("test-relino", ZK_CONNECT_STR, dataSource);
        relinoConfig.setExecutorJobQueueSize(100);

        // create action
        String sendSmsActionId = "sendSms";
        relinoConfig.registerAction(sendSmsActionId, new SendSms());

        Relino relino = new Relino(relinoConfig);

        long timeMillis = System.currentTimeMillis();
        int n = 0;

        while (true) {
            try {

                JobAttr initAttr = new JobAttr();
                initAttr.setString("userId", "orange" + System.currentTimeMillis());
                initAttr.setString("sendData", "Hello, Test Relino.");

                Job job = relino.jobProducer.builder(sendSmsActionId)
                        .jobCode("test")
                        .idempotentId(timeMillis + "-" + (n++))
                        .maxExecuteCount(5)
                        .retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY)
                        .delayExecute(LocalDateTime.now().plusSeconds(10))
                        .commonAttr(initAttr)
                        .build();

                relino.jobProducer.createJob(job);
                log.info("crate job success, jobId = {}", job.getJobId());
                Thread.sleep(50);
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
