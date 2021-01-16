package com.relino.core.task;

import com.relino.core.JobFactory;
import com.relino.core.helper.TestHelper;
import com.relino.core.model.Job;
import com.relino.core.model.retry.IRetryPolicyManager;
import com.relino.core.support.db.DBExecutor;
import com.relino.core.support.db.JobStore;
import com.relino.core.support.id.IdGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class ScanRunnableDelayJobTest {

    private JobFactory jobProducer;
    private ScanRunnableDelayJob scanRunnableDelayJob;

    @Before
    public void setUp() throws Exception {
        TestHelper.testBootStrap();
        DBExecutor dbExecutor = TestHelper.getDBExecutor();
        IdGenerator idGenerator = TestHelper.getIdGenerator();
        jobProducer = new JobFactory(new JobStore(dbExecutor), idGenerator);
        scanRunnableDelayJob = new ScanRunnableDelayJob(dbExecutor, 10);

        // 创建测试数据
        for (int i = 0; i < 5000; i++) {

            Job job = jobProducer.builder(TestHelper.LOG_ACTION_ID)
                    .retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY)
                    .maxExecuteCount(10)
                    .delayExecute(10 + ThreadLocalRandom.current().nextInt(1000)).build();
            jobProducer.createJob(job);

        }
    }

    @Test
    public void test() {
        scanRunnableDelayJob.execute();
    }
}
