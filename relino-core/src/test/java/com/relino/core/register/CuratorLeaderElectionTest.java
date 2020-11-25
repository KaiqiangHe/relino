package com.relino.core.register;

import com.relino.core.support.AbstractRunSupport;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.retry.RetryNTimes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class CuratorLeaderElectionTest {

    private static final Logger log = LoggerFactory.getLogger(CuratorLeaderElectionTest.class);

    private static final int SESSION_TIMEOUT = 30 * 1000;
    private static final int CONNECTION_TIMEOUT = 3 * 1000;
    private static final String CONNECT_STR = "127.0.0.1:2181";

    @Test
    public void testLeaderElection() throws IOException {
        RetryPolicy retryPolicy = new RetryNTimes(10, 100);
        for (int i = 0; i < 10; i++) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(
                    CONNECT_STR, SESSION_TIMEOUT, CONNECTION_TIMEOUT, retryPolicy);
            client.start();
            log.info("zk-{} connect success.", i);
            String name = "apple-" + i;
            RelinoLeaderElectionListener appleListener = new RelinoLeaderElectionListener("apple", "/relino/apple", () -> new Apple(name));
            CuratorLeaderElection leaderElection = new CuratorLeaderElection(Arrays.asList(appleListener), client);
            leaderElection.execute();
        }

        System.out.println("Press enter/return to quit\n");
        new BufferedReader(new InputStreamReader(System.in)).readLine();

        log.info("end .... ");
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
            for (int i = 0; i < 10; i++) {
                if(wannaStop()) {
                    break;
                }

                log.info("{} execute {}", name, i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if(ThreadLocalRandom.current().nextInt(3) == 0) {
                    log.info("{} mock stop", name);
                    this.terminalAsync();
                }
            }
        }

        @Override
        public void destroy() {
            // do nothing.
        }
    }

}
