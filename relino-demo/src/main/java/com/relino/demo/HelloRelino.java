package com.relino.demo;

import com.relino.core.Relino;
import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.task.JobFactory;
import com.relino.demo.action.SayHello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * @author kaiqiang.he
 */
public class HelloRelino {

    private static final Logger log = LoggerFactory.getLogger(HelloRelino.class);

    public static void main(String[] args) throws InterruptedException {

        // 2. 创建Relino
        String appId = "hello-relino";
        String zkConnectStr = RelinoDemoHelper.demoConfig.getZkConnectStr();
        DataSource dataSource = getDataSource();

        RelinoConfig relinoConfig = new RelinoConfig(appId, zkConnectStr, dataSource);
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

        // 3.2 创建Job并延10s迟执行
        Job job = jobFactory.builder(sayHelloActionId)
                .commonAttr(initAttr)
                .delayExecute(10)
                .build();
        jobFactory.createJob(job);

        Thread.sleep(15 * 1000);

        // 4. 结束Relino
        relino.shutdown();
    }

    private static DataSource getDataSource() {
        return RelinoDemoHelper.newDataSource();
    }
}
