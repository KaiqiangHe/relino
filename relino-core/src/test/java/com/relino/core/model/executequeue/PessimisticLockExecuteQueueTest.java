package com.relino.core.model.executequeue;

import com.relino.core.helper.TestHelper;
import com.relino.core.db.Store;
import com.relino.core.model.Job;
import com.relino.core.model.JobStatus;
import com.relino.core.support.id.IdGenerator;
import com.relino.core.support.id.UUIDIdGenerator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

// TODO: 2020/11/22  需要更充分的测试
public class PessimisticLockExecuteQueueTest {

    private static final Logger log = LoggerFactory.getLogger(PessimisticLockExecuteQueueTest.class);

    private ExecuteQueue executeQueue;
    private Store store;
    private IdGenerator idGenerator;

    @Before
    public void setUp() throws Exception {
        TestHelper.testBootStrap();
        idGenerator = new UUIDIdGenerator();
        store = TestHelper.getStore();
        executeQueue = new PessimisticLockExecuteQueue(store);

        // 创建测试数据
        log.info("create mock data begin ... ");
        for (int i = 0; i < 1000; i++) {
            Job job = TestHelper.getJob(idGenerator, TestHelper.LOG_ACTION_ID);
            job.setJobStatus(JobStatus.RUNNABLE);
            store.insertJob(job);
        }
        log.info("create mock data end ... ");
    }

    @Test
    public void launch() throws Exception {

        while(true) {
            List<Job> nextExecutableJob = executeQueue.getNextExecutableJob(20);
            if(nextExecutableJob.isEmpty()) {
                break;
            }
            nextExecutableJob.forEach(j -> log.info("job {}", j));
        }

        log.info("end ....... ");
    }
}
