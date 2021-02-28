package com.relino.spring.demo.controller;

import com.relino.core.Relino;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.task.JobFactory;
import com.relino.spring.RelinoAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * @author kaiqiang.he
 */
@RestController
public class HelloController {

    @Resource
    private RelinoAutoConfiguration relinoAutoConfiguration;

    @Resource
    private DataSource dataSource;

    @Resource
    private Relino relino;

    @RequestMapping("/hello")
    public String say() {
        JobFactory jobFactory = relino.getJobFactory();

        JobAttr initAttr = new JobAttr();
        initAttr.setString("userId", "orange" + System.currentTimeMillis());

        Job job = jobFactory.builder("sayHello")
                .idempotentId(System.currentTimeMillis() + "")
                .maxExecuteCount(5)
                .retryPolicy(Relino.IMMEDIATELY_RETRY_POLICY)
                .delayExecute(LocalDateTime.now().plusSeconds(10))
                .commonAttr(initAttr)
                .build();
        jobFactory.createJob(job);

        return "hello!!!";
    }

}
