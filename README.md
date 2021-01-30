# relino

**reliable-notice** 基于mysql的轻量级无中心化可靠执行 & 延时(定时)执行。

### 快速开始

#### 创建数据表

执行`/relino/sql/init.sql`文件中的sql语句。


#### Maven
```
<dependency>
    <groupId>org.github.relino</groupId>
    <artifactId>relino-core</artifactId>
    <version>1.0</version>
</dependency>
```
#### HelloRelino

参考`/relino/relino-demo/src/main/java/com/relino/demo/helloworld/HelloRelino.java`类

##### 1. 创建Action
```java
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
```

##### 2. 创建Relino & 注册Action & 启动
```
String appId = "hello-relino";
String ZK_CONNECT_STR = "127.0.0.1:2181";   // zk地址
DataSource dataSource = ...                 // 数据源 

RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);

// 注册 Action
String sayHelloActionId = "sayHello";
relinoConfig.registerAction(sayHelloActionId, new SayHello());

// 创建 & 启动
Relino relino = new Relino(relinoConfig);
relino.start();
```

##### 3. 创建Job & 延迟执行
```
JobFactory jobFactory = relino.getJobFactory();

// Job属性
JobAttr initAttr = new JobAttr();
initAttr.setString("userId", "orange" + System.currentTimeMillis());

Job job = jobFactory.builder(sayHelloActionId).commonAttr(initAttr).delayExecute(10).build();
jobFactory.createJob(job);
```

##### 4. 关闭Relino
```
relino.shutdown();
```

### 配置介绍

| 设置名                   | 默认值                                                       | 说明                                                         |
| :----------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| appId                    | 无，需指定                                                   | 应用唯一id                                                   |
| zkConnectStr             | 无，需指定                                                   | 使用zookeeper作为注册中心，该属性为连接地址，如果为单机模式如`127.0.0.1:2181`；集群模式如`127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181` |
| dataSource               | 无，需指定                                                   | DataSource 数据源                                            |
| executorJobCoreThreadNum | 5                                                            | 执行Job核心线程数                                            |
| executorJobMaxThreadNum  | 20                                                           | 执行Job最大线程数                                            |
| executorJobQueueSize     | 2000                                                         | 缓存将要执行Job的队列大小                                    |
| idGenerator              | 默认为`TimeHostPidIdGenerator`，基于当前时间、本地ip、进程pid生成 | `jobId`生成器，目前有`TimeHostPidIdGenerator`和`UUIDIdGenerator`两种，可实现`<<IdGenerator>>` 接口，通过`setIdGenerator()`来指定id生成器。 |
| actionMap                | 无，需自行注册                                               | 注册的`Action`，可以通过`registerAction(String actionId, Action action)`注册 |
| selfRetryPolicy          | 默认一注册了`ImmediatelyRetryPolicy` 立即重试，为`Relino#IMMEDIATELY_RETRY_POLICY` | 自定义重试策略，可通过`registerRetryPolicy(String retryPolicyId, IRetryPolicy retry)`注册自定义重试策略。 |
| defaultRetryPolicy       | `LinearRetryPolicy` - 重试时间线性增长策略，为5乘以已执行的次数，为5 10 15 20 ... | 默认的重试策略，为`Relino#DEFAULT_RETRY_POLICY`常量          |

配置可参考`/relino/relino-demo/src/main/java/com/relino/demo/RelinoConfigDemo.java`

```java
public class RelinoConfigDemo {

    /**
     * Relino配置Demo
     */
    public static void main(String[] args) {

        String appId = "relino-config-demo";
        String ZK_CONNECT_STR = "127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181";     // 设置为集群模式
        DataSource dataSource = null;   // 指定datasource

        RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);

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
```

### 创建Job

```java
JobFactory jobFactory = relino.getJobFactory();

// 初始化job属性
// 支持String long double boolean LocalDateTime
JobAttr initAttr = new JobAttr();
 initAttr.setString("userId", "orange" + System.currentTimeMillis());
initAttr.setLocalDateTime("createTime", LocalDateTime.now());
initAttr.setDouble("double-value", 0.1);
initAttr.setLong("long-value", 100);
 initAttr.setBoolean("boolean-value", false);

// 创建Job
Job job = jobFactory.builder("sayHello")
                .idempotentId("sayHello-001")                   // 设置幂等id
                .delayExecute(10)                              				 // 延迟10s执行
                .maxExecuteCount(5)                             		// 设置最大重试次数为5
                .retryPolicy(Relino.IMMEDIATELY_RETRY_POLICY)   // 设置重试策略
                .commonAttr(initAttr)                           	// 设置job属性
                .build();
 jobFactory.createJob(job);
```

## RoadMap

| Item                                                         |
| ------------------------------------------------------------ |
| 重构存储结构，采用一个Action一个表的方式，支持单Action单台机器速度限制 |
| 后台管理系统，支持Action的创建、Job增删改查等                |
|                                                              |

