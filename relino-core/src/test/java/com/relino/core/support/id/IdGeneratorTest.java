package com.relino.core.support.id;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class IdGeneratorTest {

    private static final Logger log = LoggerFactory.getLogger(IdGeneratorTest.class);

    @Test
    public void testUUIDGenerator() throws InterruptedException {
        testIdGenerator(new UUIDIdGenerator());
    }

    @Test
    public void testTimeHostPidIdGenerator() throws InterruptedException {
        testIdGenerator(new TimeHostPidIdGenerator());
    }

    private void testIdGenerator(IdGenerator idGenerator) throws InterruptedException {
        testGetNext(idGenerator);
        multiTest(idGenerator);
    }

    private void testGetNext(IdGenerator idGenerator) {
        String id = idGenerator.getNext();
        log.info("id = {}", id);
        Assert.assertNotNull(id);
    }

    private void multiTest(IdGenerator idGenerator) throws InterruptedException {

        int nThread = 30;
        int count = 40000;
        CountDownLatch countDownLatch = new CountDownLatch(nThread);
        Set<String> idHolders = Collections.synchronizedSet(new HashSet<>());
        Object obj = new Object();

        long start = System.currentTimeMillis();
        for (int i = 0; i < nThread; i++) {
            new Thread(() -> {
                for (int k = 0; k < count; k++) {
                    String id = idGenerator.getNext();
                    synchronized (obj) {
                        Assert.assertFalse(idHolders.contains(id));
                        idHolders.add(id);
                    }
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

        Assert.assertEquals(nThread * count, idHolders.size());

        log.info("TotalCount = {}, time = {}ms", nThread * count, System.currentTimeMillis() - start);
    }
}
