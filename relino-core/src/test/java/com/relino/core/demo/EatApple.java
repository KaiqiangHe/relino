package com.relino.core.demo;

import com.relino.core.Relino;
import com.relino.core.model.*;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author kaiqiang.he
 */
public class EatApple implements Action {

    @Override
    public ActionResult execute(String jobId, JobAttr commonAttr, int executeCount) {

        System.out.println("EatApple execute. jobId = " + jobId + ", executeCount = " + executeCount);
        return ActionResult.buildSuccess();
    }

    public void doSomething() {

        Relino relino = new Relino(null, 100, 100, 5);

        String actionId = "eatApple";

        // 注册Action
        ActionManager.register(actionId, new EatApple());

        // 创建Oper
        Oper mOper = Oper.builder(actionId).retryPolicy("_d").maxExecuteCount(3).build();

        // 创建Job属性
        JobAttr initAttr = new JobAttr();
        initAttr.setString("appleId", "apple-" + System.currentTimeMillis());
        initAttr.setLocalDateTime("crateTime", LocalDateTime.now());

        // 创建Job
        BaseJob job = relino.jobProducer.builder(mOper)
                .commonAttr(initAttr)
                .delayJob(10 + ThreadLocalRandom.current().nextInt(100))
                .build();



    }

}
