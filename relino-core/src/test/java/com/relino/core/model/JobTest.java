package com.relino.core.model;

import com.relino.core.JobProducer.JobBuilder;
import com.relino.core.helper.LogAction;
import com.relino.core.helper.TestHelper;
import com.relino.core.model.retry.IRetryPolicyManager;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.UUIDIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobTest {
    
    private static final Logger log = LoggerFactory.getLogger(JobTest.class);

    private static IdGenerator idGenerator = new UUIDIdGenerator();

    @Before
    public void init() {
        TestHelper.testBootStrap();
    }

    @Test
    public void testCreateJob() {
        Oper oper = Oper.builder(TestHelper.LOG_ACTION_ID).maxExecuteCount(10).retryPolicy(IRetryPolicyManager.IMMEDIATELY_RETRY_POLICY).build();
        JobAttr commonAttr = new JobAttr();
        commonAttr.setString(LogAction.logValue, "hello-" + System.currentTimeMillis());
        Job job = new JobBuilder(idGenerator.getNext(), oper).delayJob(100).commonAttr(commonAttr).build();
        log.info("end ...... ");
    }
}