# Relino

**Relino**是一个用java语言开发，基于数据库存储、轻量级、无中心化的可靠执行和延时(定时)执行组件。不需要复杂的安装过程，只需在业务项目中初始化一些数据表，开箱即用，在一些**请求量不高**的场景中，可以作为消息中间件**部分功能**的替代方案。

该项目正在完善和测试中，有一个beta版本可供体验。如果你对项目感兴趣（开发测试等等），或者有什么建议，欢迎参与到Relino。

### 快速开始

项目中包含了`relino-demo`模块，可以配置`/src/main/profile/*/config.properties`，选择对应的`profile`，就可以直接运行该模块下的例子。

#### 1. 初始化

组件使用数据库作为存储，`Zookeeper`作为配置中心，需在业务数据库中执行`/relino/sql/init.sql`。[除此之外，也可以通过docker快速搭建数据库和ZooKeeper环境](./doc/docker-quick-start.md)


#### 2. Maven
项目还在完善和测试中，可以`git clone` 该项目运行`relino-demo`下的例子体验。

#### 3. Java

可以参考`/relino/relino-demo/src/main/java/com/relino/demo/helloworld/HelloRelino.java`

##### 3.1 实现Action接口，创建SayHello类
```java
public class SayHello implements Action {
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

##### 3. 2 创建Relino、注册Action、启动
```
String appId = "hello-relino";			// 应用唯一标识
String zkConnectStr = ...; 			// zk地址，例如 127.0.0.1:2181
DataSource dataSource = ... ;		// 数据源 

RelinoConfig relinoConfig = new RelinoConfig(appId, zkConnectStr, dataSource);

// 注册 Action
String sayHelloActionId = "sayHello";
relinoConfig.registerAction(sayHelloActionId, new SayHello());

// 创建 & 启动
Relino relino = new Relino(relinoConfig);
relino.start();
```

##### 3.3 创建Job & 延迟执行
```
JobFactory jobFactory = relino.getJobFactory();

// Job属性
JobAttr initAttr = new JobAttr();
initAttr.setString("userId", "orange" + System.currentTimeMillis());

Job job = jobFactory.builder(sayHelloActionId).commonAttr(initAttr).delayExecute(10).build();
jobFactory.createJob(job);
```

##### 3.4 关闭Relino
```
relino.shutdown();
```

### 系统设计
[系统设计](./doc/architecture-uml.md)

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
        String zkConnectStr = "127.0.0.1:2181,127.0.0.2:2181,127.0.0.3:2181";     // 设置为集群模式
        DataSource dataSource = null;   // 指定datasource

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
```

### 创建Job API

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

## Road Map

* 支持常见的数据库，如`Mysql` ` SqlServer ` `Oracle` ` PostgreSQL` 等
* 后台管理系统，支持Job的查找，创建，重新执行等功能
* 接入`SpringBoot`，可以更方便的使用组件