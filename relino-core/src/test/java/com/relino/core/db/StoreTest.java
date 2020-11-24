package com.relino.core.db;

import com.relino.core.helper.TestHelper;
import com.relino.core.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;

public class StoreTest {
    
    private static final Logger log = LoggerFactory.getLogger(StoreTest.class);

    private Store store;

    @Before
    public void init() {
        store = new DBBasedStore(TestHelper.getDataSource());
    }

    @Test
    public void insertJob() throws SQLException {
        Job job = createTestJob();
        store.insertJob(job);

        JobEntity entity = store.queryByJobId(job.getJobId());
        Assert.assertNotNull(entity);
    }

    @Test
    public void testUpdateJob() throws SQLException {
        Job job = createTestJob();
        store.insertJob(job);

        job.setJobStatus(JobStatus.FINISHED);
        job.setExecuteOrder(1);
        job.setWillExecuteTime(LocalDateTime.now().plusYears(1));

        JobAttr resultValue = new JobAttr();
        resultValue.setString("hello", "world");
        job.getCommonAttr().addAll(resultValue);

        Oper mOper = job.getMOper();
        mOper.setOperStatus(OperStatus.FAILED_FINISHED);
        mOper.setExecuteCount(2);

        store.updateJob(job, false);

        store.updateJob(job, true);
    }

    private Job createTestJob() {
        Oper oper = new Oper("test-action-id", OperStatus.RUNNABLE,
                1, 3, "test-retry-policy-id");
        Job job = new Job(null, "id" + System.currentTimeMillis(), "id" + System.currentTimeMillis(),
                "test-job-code", true, LocalDateTime.now(), new JobAttr(), oper);
        job.setExecuteOrder(-1);
        job.setJobStatus(JobStatus.DELAY);
        job.setWillExecuteTime(LocalDateTime.now());

        return job;
    }

    // ----------------------------------------------------------------------
    // tx test
    @Test
    public void testTxRollback() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();

        mockTx(job1, job2, true);

        Assert.assertNull(store.queryByJobId(job1.getJobId()));
        Assert.assertNull(store.queryByJobId(job2.getJobId()));
    }

    @Test
    public void testTxCommit() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();

        mockTx(job1, job2, false);

        Assert.assertNotNull(store.queryByJobId(job1.getJobId()));
        Assert.assertNotNull(store.queryByJobId(job2.getJobId()));
    }

    @Test
    public void testNotUseTx() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();

        try {
            store.insertJob(job1);
            if(1 == 1) {
                throw new RuntimeException("mock exception");
            }
            store.insertJob(job2);
        } catch (Exception e) {
            log.error("error ", e);
        }

        Assert.assertNotNull(store.queryByJobId(job1.getJobId()));
        Assert.assertNull(store.queryByJobId(job2.getJobId()));
    }

    private void mockTx(Job job1, Job job2, boolean exception) throws SQLException {
        store.beginTx();
        try {
            store.insertJob(job1);
            if(exception) {
                throw new RuntimeException("mock exception");
            }
            store.insertJob(job2);
            store.commitTx();
        } catch (Exception e) {
            store.rollbackTx();
            log.error("error ", e);
        }
    }

    @Test
    public void testExecuteWithTxCommit() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();

        mocExecuteWithTx(job1, job2, false);

        Assert.assertNotNull(store.queryByJobId(job1.getJobId()));
        Assert.assertNotNull(store.queryByJobId(job2.getJobId()));
    }

    @Test
    public void testExecuteWithTxRollback() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();

        mocExecuteWithTx(job1, job2, true);

        Assert.assertNull(store.queryByJobId(job1.getJobId()));
        Assert.assertNull(store.queryByJobId(job2.getJobId()));
    }

    private void mocExecuteWithTx(Job job1, Job job2, boolean exception) {
        try {
            store.executeWithTx(jobs -> {
                store.insertJob(jobs.get(0));
                if(exception) {
                    throw new RuntimeException("mock exception");
                }
                store.insertJob(jobs.get(1));

                return null;
            }, Arrays.asList(job1, job2));

        } catch (Exception e) {
            log.error("error ", e);
        }
    }
}





















