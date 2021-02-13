package com.relino.demo;

import com.relino.core.config.RelinoConfig;
import com.relino.core.model.retry.IRetryPolicy;
import com.relino.core.model.retry.LinearRetryPolicy;
import com.relino.core.support.id.UUIDIdGenerator;

import javax.sql.DataSource;

/**
 * @author kaiqiang.he
 */
public class RelinoConfigDemo {

    /**
     * Relino配置Demo
     */
    public static void main(String[] args) {

        String appId = "relino-config-demo";
        String zkConnectStr = "127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181";     // ZK连接字符串 集群模式
        DataSource dataSource = null;                                               // 指定datasource

        RelinoConfig relinoConfig = new RelinoConfig(appId, zkConnectStr, dataSource);

        // 设置执行job的核心线程数为3，最大线程数为10
        // 设置缓存需要执行Job的队列为1000
        relinoConfig.setExecutorJobCoreThreadNum(3);
        relinoConfig.setExecutorJobMaxThreadNum(10);
        relinoConfig.setExecutorJobQueueSize(1000);

        // 设置id生成器为UUIDIdGenerator
        relinoConfig.setIdGenerator(new UUIDIdGenerator());

        // 注册Action
        relinoConfig.registerAction("sayHello", new Main.SayHello());

        // 注册自定义重试策略
        relinoConfig.registerRetryPolicy("im_then_delay", new ImmediatelyThenDelayRetryPolicy());

        // 设置默认重试策略为 为3乘以已执行的次数 + 5
        relinoConfig.setDefaultRetryPolicy(new LinearRetryPolicy(3, 5));
    }

    /**
     * 前3次立即重试，之后延迟 5 * executeCount秒
     */
    static class ImmediatelyThenDelayRetryPolicy implements IRetryPolicy {

        @Override
        public int retryAfterSeconds(int executeCount) {
            if(executeCount <= 3) {
                return 0;
            } else {
                return 5 * executeCount;
            }
        }
    }
}
