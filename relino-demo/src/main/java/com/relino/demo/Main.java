package com.relino.demo;

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

    /*private static final Logger log = LoggerFactory.getLogger(Main.class);

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

        String appId = "hello-relino";
        String ZK_CONNECT_STR = "127.0.0.1:2181";
        RelinoConfig relinoConfig = new RelinoConfig(appId, ZK_CONNECT_STR, dataSource);

        // 注册 Action
        String sayHelloActionId = "sayHello";
        // relinoConfig.registerAction(sayHelloActionId, new SayHello());

        Relino relino = new Relino(relinoConfig);
        relino.start();

        JobFactory jobFactory = relino.getJobFactory();

        long timeMillis = System.currentTimeMillis();
        int n = 0;
        while (n < 1000) {
            try {

                JobAttr initAttr = new JobAttr();
                initAttr.setString("userId", "orange" + System.currentTimeMillis());

                Job job = jobFactory.builder(sayHelloActionId)
                        .idempotentId(timeMillis + "-" + (n))                           // 幂等id
                        .maxExecuteCount(5)                                             // 最大重试次数
                        .retryPolicy(Relino.IMMEDIATELY_RETRY_POLICY)                   // 重试策略
                        .delayExecute(LocalDateTime.now().plusSeconds(10))              // 指定时间执行
                        .commonAttr(initAttr)                                           // 设置job属性
                        .build();

                jobFactory.createJob(job);

                log.info("create job success, jobId = {}", job.getJobId());
                Thread.sleep(5);

                n++;
            } catch (Exception e) {
                log.error("create job error ", e);
            }
        }

        relino.shutdown();
    }*/

    /*static class SayHello implements Action {

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
    }*/
}
