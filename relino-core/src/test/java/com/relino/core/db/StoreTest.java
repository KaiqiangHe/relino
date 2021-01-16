package com.relino.core.db;

/**
 * todo 提取有用的测试用例
 */
public class StoreTest {
    
    /*private static final Logger log = LoggerFactory.getLogger(StoreTest.class);

    private Store store;

    private IdGenerator idGenerator = new UUIDIdGenerator();

    @Before
    public void init() {
        store = new DBBasedStore(TestHelper.getDataSource());
    }

    @Test
    public void testInsertAndQuery() throws SQLException {
        Job job = createTestJob();
        store.insertJob(job);

        JobEntity entity = store.queryByJobId(job.getJobId());
        Job newJob = JobEntity.toJob(entity);
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

        String id = idGenerator.getNext();
        Job job = new Job(null, id, id, "test-job-code", true, LocalDateTime.now(),
                JobStatus.RUNNABLE, -1, LocalDateTime.now(),
                new JobAttr(), oper);
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

    // -------------------------------------------------------------------

    @Test
    public void testSetDelayJobRunnable() throws SQLException {
        Job job1 = createTestJob();
        Job job2 = createTestJob();
        long job1ExecuteOrder = System.currentTimeMillis();
        long job2ExecuteOrder = System.currentTimeMillis() + 1;
        store.insertJob(job1);
        store.insertJob(job2);

        List<IdAndExecuteOrder> elems = Arrays.asList(
            new IdAndExecuteOrder(store.queryByJobId(job1.getJobId()).getId(), job1ExecuteOrder),
            new IdAndExecuteOrder(store.queryByJobId(job2.getJobId()).getId(), job2ExecuteOrder)
        );

        store.setDelayJobRunnable(elems);
        Assert.assertEquals(job1ExecuteOrder, store.queryByJobId(job1.getJobId()).getExecuteOrder());
        Assert.assertEquals(job2ExecuteOrder, store.queryByJobId(job2.getJobId()).getExecuteOrder());
    }

    // -------------------------------------------------------------------

    // TODO: 2020/11/26 待充分测试
    @Test
    public void testGetRunnableDelayJobId() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowPlus1H = now.plusHours(1);
        LocalDateTime nowPlus2H = now.plusHours(2);
        LocalDateTime nowPlus3H = now.plusHours(3);

        Job job1 = createTestJob();
        Job job2 = createTestJob();
        Job job3 = createTestJob();
        Job job4 = createTestJob();

        job1.setWillExecuteTime(now);
        job1.setJobStatus(JobStatus.DELAY);
        job2.setWillExecuteTime(nowPlus1H);
        job2.setJobStatus(JobStatus.DELAY);
        job3.setWillExecuteTime(nowPlus2H);
        job3.setJobStatus(JobStatus.DELAY);
        job4.setWillExecuteTime(nowPlus3H);
        job4.setJobStatus(JobStatus.DELAY);

        store.insertJob(job3);
        store.insertJob(job4);
        store.insertJob(job1);
        store.insertJob(job2);

        long job1Id = store.queryByJobId(job1.getJobId()).getId();
        long job2Id = store.queryByJobId(job2.getJobId()).getId();
        long job3Id = store.queryByJobId(job3.getJobId()).getId();
        long job4Id = store.queryByJobId(job4.getJobId()).getId();

        List<Long> list;
        list = store.getRunnableDelayJobId(now, nowPlus3H, 10);

        list = store.getRunnableDelayJobId(nowPlus1H, nowPlus2H, 2);
        Assert.assertEquals(job2Id, list.get(0).longValue());
        Assert.assertEquals(job3Id, list.get(1).longValue());

        list = store.getRunnableDelayJobId(now, nowPlus2H, 2);
        Assert.assertEquals(job1Id, list.get(0).longValue());
        Assert.assertEquals(job2Id, list.get(1).longValue());
    }

    @Test
    public void testSelectDeadJobByTime() throws SQLException {
        LocalDateTime time = LocalDateTime.of(2011, 1, 1, 0, 0, 0, 0);
        store.insertExecuteRecord(10, time);
        ExecuteTimeEntity entity = store.selectDeadJobByTime(time.minusMinutes(10));
        store.deleteExecuteTimeRecord(entity.getId());
        entity = store.selectDeadJobByTime(time.minusMinutes(10));

        log.info("end ... ");
    }*/
}





















