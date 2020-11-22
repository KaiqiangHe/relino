package com.relino.core.register;

import com.relino.core.support.AbstractRunSupport;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class CuratorLeaderElectionTest {

    private static final Logger log = LoggerFactory.getLogger(CuratorLeaderElectionTest.class);

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;
    private static final String CONNECT_STR = "127.0.0.1:2181";

    private CuratorFramework curatorClient;

    @Before
    public void setUp() throws Exception {
        RetryPolicy retryPolicy = new RetryNTimes(10, 100);
        curatorClient = CuratorFrameworkFactory.newClient(
                CONNECT_STR, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
        curatorClient.start();
    }

    @After
    public void tearDown() throws Exception {
        curatorClient.close();
    }

    @Test
    public void testLeaderElection() {
        RelinoLeaderElectionListener appleListener = new RelinoLeaderElectionListener(
            "apple", "/relino/apple", () -> new Apple("apple")
        );
        CuratorLeaderElection leaderElection = new CuratorLeaderElection(Arrays.asList(appleListener), curatorClient);
        leaderElection.execute();

        log.info("end ... ");
    }

    static class Apple extends AbstractRunSupport implements ElectionCandidate {

        private String name;

        public Apple(String name) {
            this.name = name;
        }

        @Override
        public void executeWhenCandidate() throws Exception {
            execute();
        }

        @Override
        public void stopExecute() {
            terminal();
        }

        @Override
        protected void execute0() {
            for (int i = 0; i < 100; i++) {
                if(wannaStop()) {
                    break;
                }

                log.info("{} execute {}", name, i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void destroy() {
            // do nothing.
        }
    }

}
