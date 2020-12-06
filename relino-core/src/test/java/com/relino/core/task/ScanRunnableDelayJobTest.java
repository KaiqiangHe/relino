package com.relino.core.task;

import com.relino.core.JobProducer;
import com.relino.core.db.Store;
import com.relino.core.helper.TestHelper;
import com.relino.core.model.Job;
import com.relino.core.model.Oper;
import com.relino.core.model.retry.IRetryPolicyManager;
import com.relino.core.support.id.IdGenerator;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;

public class ScanRunnableDelayJobTest {

    private JobProducer jobProducer;
    private ScanRunnableDelayJob scanRunnableDelayJob;

    @Before
    public void setUp() throws Exception {
        TestHelper.testBootStrap();
        Store store = TestHelper.getStore();
        IdGenerator idGenerator = TestHelper.getIdGenerator();
        jobProducer = new JobProducer(store, idGenerator);
        scanRunnableDelayJob = new ScanRunnableDelayJob(store, 10);

        // 创建测试数据
        for (int i = 0; i < 5000; i++) {
            Oper mOper = Oper.builder(TestHelper.LOG_ACTION_ID)
                    .retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY)
                    .maxExecuteCount(10)
                    .build();
            Job job = jobProducer.builder(mOper).delayJob(10 + ThreadLocalRandom.current().nextInt(1000)).build();
            jobProducer.createJob(job);
        }
    }

    @Test
    public void test() {
        scanRunnableDelayJob.execute();
    }
}
