package com.relino.demo.helloworld;

import com.relino.core.Relino;
import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Action;
import com.relino.core.model.ActionResult;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.task.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class HelloRelino {

    private static final Logger log = LoggerFactory.getLogger(HelloRelino.class);

    // 1. 实现Action接口，创建SayHello类
    static class SayHello implements Action {

        @Override
        public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

            try {

                String userId = commonAttr.getString("userId"); // 获取Job属性
                Thread.sleep(100);
                log.info("Hello {}", userId);
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

    public static void main(String[] args) throws InterruptedException {

        // 2. 创建Relino
        String appId = "hello-relino";
        String ZK_CONNECT_STR = "127.0.0.1:2181";
        DataSource dataSource = getDataSource();

        RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);
        // 2.1 注册 Action
        String sayHelloActionId = "sayHello";
        relinoConfig.registerAction(sayHelloActionId, new SayHello());

        // 2.2 创建Relino并启动
        Relino relino = new Relino(relinoConfig);
        relino.start();

        // 3. 创建Job
        JobFactory jobFactory = relino.getJobFactory();

        // 3.1 Job属性
        JobAttr initAttr = new JobAttr();
        initAttr.setString("userId", "orange" + System.currentTimeMillis());

        // 3.2 创建Job并延5s迟执行
        Job job = jobFactory.builder(sayHelloActionId)
                .commonAttr(initAttr)
                .delayExecute(10)
                .build();
        jobFactory.createJob(job);

        Thread.sleep(10 * 1000);

        // 4. 结束Relino
        relino.shutdown();
    }

    private static DataSource getDataSource() {
        throw new RuntimeException("请指定数据源");
    }

}
