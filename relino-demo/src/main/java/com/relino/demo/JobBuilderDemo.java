package com.relino.demo;

import com.relino.core.Relino;
import com.relino.core.config.RelinoConfig;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.task.JobFactory;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
public class JobBuilderDemo {

    public static void main(String[] args) {

        String appId = "relino-config-demo";
        String ZK_CONNECT_STR = "127.0.0.1:2181";     // 设置为集群模式
        DataSource dataSource = null;   // 指定datasource

        RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);
        Relino relino = new Relino(relinoConfig);

        JobFactory jobFactory = relino.getJobFactory();

        // 初始化job属性
        JobAttr initAttr = new JobAttr();
        initAttr.setString("userId", "orange" + System.currentTimeMillis());
        initAttr.setLocalDateTime("createTime", LocalDateTime.now());
        initAttr.setDouble("double-value", 0.1);
        initAttr.setLong("long-value", 100);
        initAttr.setBoolean("boolean-value", false);

        // 创建Job
        Job job = jobFactory.builder("sayHello")
                .idempotentId("sayHello-001")                   // 设置幂等id
                .delayExecute(10)                               // 延迟10s执行
                .maxExecuteCount(5)                             // 设置最大重试次数为5
                .retryPolicy(Relino.IMMEDIATELY_RETRY_POLICY)   // 设置重试策略
                .commonAttr(initAttr)                           // 设置job属性
                .build();
        jobFactory.createJob(job);
    }
}
