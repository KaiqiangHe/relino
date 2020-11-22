package com.relino.core;

import com.relino.core.db.DBBasedStore;
import com.relino.core.db.Store;
import com.relino.core.helper.TestHelper;
import com.relino.core.model.Job;
import com.relino.core.model.JobAttr;
import com.relino.core.model.Oper;
import com.relino.core.model.OperStatus;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.UUIDIdGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JobProducerTest {

    private JobProducer jobProducer;

    @Before
    public void setUp() throws Exception {
        IdGenerator idGenerator = new UUIDIdGenerator();
        Store store = new DBBasedStore(TestHelper.getDataSource());
        jobProducer = new JobProducer(store, idGenerator);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createJob() {

        Oper oper = new Oper("test-action-id", OperStatus.RUNNABLE,
                1, 3, "test-retry-policy-id");

        JobAttr commonAttr = new JobAttr();
        commonAttr.setString("hello", "world" + System.currentTimeMillis());

        Job job = jobProducer.builder(oper).commonAttr(commonAttr).delayJob(100).build();
        jobProducer.createJob(job);
    }
}
