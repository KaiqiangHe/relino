package com.relino.core.learn;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author kaiqiang.he
 */
public class LearnScheduledExecutorService {

    private static final Logger log = LoggerFactory.getLogger(LearnScheduledExecutorService.class);

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Test
    public void testScheduleAtFixedRate() throws ExecutionException, InterruptedException {

        ScheduledFuture<?> future = executor.scheduleAtFixedRate(() -> {

            int mockExecuteSeconds = ThreadLocalRandom.current().nextInt(1000) + 500;
            log.info("start execute ... {}", mockExecuteSeconds);
            try {
                Thread.sleep(mockExecuteSeconds);
            } catch (InterruptedException e) {
                log.error("error ", e);
            }
            log.info("end execute ...");

        }, 0, 1, TimeUnit.SECONDS);

       log.info("end ...");
    }
}
