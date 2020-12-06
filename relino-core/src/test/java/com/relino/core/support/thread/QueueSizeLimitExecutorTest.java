package com.relino.core.support.thread;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class QueueSizeLimitExecutorTest {

    private static final Logger log = LoggerFactory.getLogger(QueueSizeLimitExecutorTest.class);

    @Test
    public void test() throws InterruptedException {

        QueueSizeLimitExecutor<Apple> executor = new QueueSizeLimitExecutor<>("apple", 1, 2, 10);

        int tCount = 3;
        CountDownLatch countDownLatch = new CountDownLatch(tCount);
        for (int i = 0; i < tCount; i++) {
            String name = i + "";
            new Thread(() -> {
                int count = 0;
                while (true) {
                    if(count >= 100) {
                        break;
                    }
                    Apple apple = new Apple(1000, name);
                    if(executor.addItem(apple)) {
                        count++;
                    }
                }
                countDownLatch.countDown();
            }).start();
        }
        countDownLatch.await();

        log.info("end.....");
    }

    private static class Apple implements Processor {

        private int delayMillSeconds;

        private String name;

        public Apple(int delayMillSeconds, String name) {
            this.delayMillSeconds = delayMillSeconds;
            this.name = name;
        }

        @Override
        public void process() {
            try {
                Thread.sleep(delayMillSeconds);
                log.info("{} execute end.", name);
            } catch (InterruptedException e) {
                log.error("error ", e);
            }
        }
    }
}
