package com.relino.core.register;

import com.relino.core.config.LeaderSelectorConfig;
import com.relino.core.exception.HandleException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于CuratorLeader的Leader节点选取
 *
 * 参考 elasticjob ZookeeperElectionService 重新实现
 *
 * @author kaiqiang.he
 */
public class CuratorLeaderElection {

    private static final Logger log = LoggerFactory.getLogger(CuratorLeaderElection.class);

    private List<RelinoLeaderElectionListener> leaderSelectorListener;
    private CuratorFramework curatorClient;
    private List<LeaderSelector> leaderSelectorList = new ArrayList<>();

    public CuratorLeaderElection(List<RelinoLeaderElectionListener> leaderSelectorListener, CuratorFramework curatorClient) {
        this.leaderSelectorListener = leaderSelectorListener;
        this.curatorClient = curatorClient;
    }

    public void execute() {
        for (RelinoLeaderElectionListener listener : leaderSelectorListener) {
            LeaderSelectorConfig config = listener.getLeaderSelectorConfig();
            LeaderSelector leaderSelector = new LeaderSelector(curatorClient, config.getLeaderPath(), listener);
            leaderSelectorList.add(leaderSelector);
            leaderSelector.autoRequeue();
            leaderSelector.start();
            log.info("{} leader selection start.", config.getName());
        }
    }

    public void shutdown() {
        log.info("开始结束CuratorLeaderElection");
        for (LeaderSelector leaderSelector : leaderSelectorList) {
            try {
                leaderSelector.close();
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
            }
        }
        log.info("结束CuratorLeaderElection完成");
    }
}
