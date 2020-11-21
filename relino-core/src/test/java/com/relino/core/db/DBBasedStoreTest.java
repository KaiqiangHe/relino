package com.relino.core.db;

import com.relino.core.TestHelper;
import com.relino.core.model.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class DBBasedStoreTest {

    private Store store;

    @Before
    public void init() {
        store = new DBBasedStore(TestHelper.getDataSource());
    }

    @Test
    public void insertJob() throws SQLException {
        Job job = createTestJob();
        store.insertJob(job);
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
}
