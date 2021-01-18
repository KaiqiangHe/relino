# relino

**reliable-notice** 基于mysql的轻量级无中心化可靠执行 & 延时(定时)执行

## 快速开始
### 1. 添加依赖
```
<dependency>
    <groupId>org.github.relino</groupId>
    <artifactId>relino-core</artifactId>
    <version>1.0</version>
</dependency>
```
### 2. 实现Action接口，创建SayHello类
```java
static class SayHello implements Action {

        @Override
        public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

            try {
                String userId = commonAttr.getString("userId");
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

###  3. 创建执行Job
```java
static class Main {

    public static void main(String[] args){
      // 创建Relino对象
      String appId = "hello-relino";
      String ZK_CONNECT_STR = "127.0.0.1:2181";
      RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);
      
      String sayHelloActionId = "sayHello";
      relinoConfig.registerAction(sayHelloActionId, new SayHello()); // 注册 Action
      
      Relino relino = new Relino(relinoConfig);
      
      // 创建Job属性
      JobAttr initAttr = new JobAttr();
      initAttr.setString("userId", "orange" + System.currentTimeMillis());
      
      // 创建Job
      Job job = relino.jobProducer.builder(sayHelloActionId)
                              .idempotentId("mock-idepotent-id")                              // 幂等id
                              .maxExecuteCount(5)                                             // 最大重试次数
                              .retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY)      // 重试策略
                              .delayExecute(LocalDateTime.now().plusSeconds(10))              // 指定延迟执行时间
                              .commonAttr(initAttr)                                           // 设置job属性
                              .build();
      relino.jobProducer.createJob(job);
    }

}
```

## RoadMap

## TODO
### 顺序执行
有`J1 J2 ... Jn Jn+1 ...`多个Job，保证Jn执行成功后才执行Jn+1。
不要求按照执行顺序创建Job，但必须指定自己执行的次序，例如：可先插入Jn再插入Jn-x (n > x > 0)。

注意：
* 会影响性能
* 如果Jn执行失败，后续执行会阻塞

#### 技术方案
目前Job只要达到执行时间就可执行，在此基础上添加额外字段int condition，需**达到执行时间 且 condition为0**才可执行。

流程如下：
1. 创建J1时，condition = 0
2. 创建Jn时，如果Jn-1已经执行成功则condition = 0；未执行则condition = 1
3. 当Jn执行成功后，将Jn+1的condition - 1

存在的问题和相应的解决方案：
1. 流程2 3存在并发问题。
解决方法： 
a. 数据库悲观锁(建议)；
b. 修改现有方案，创建Jn时condition为1，等待定时任务来更新condition(效率较低，对DB压力大)；
c. 其他方案

2. 流程2，如果Jn-1还未创建，或Jn-1正在创建

3. 流程3中包含两个操作，设置Jn执行成功和修改Jn+1的condition。
解决方法：
a. 将两个操作通过DB事务做成原子的
b. 采用补偿机制，使用定时任务处理一个成功，另一个失败的情况 -> 没必要。

====> 最终方案如下：
使用数据库悲观锁，当创建job、job执行成功更新condition时，通过事务和悲观锁解决存在的问题。
