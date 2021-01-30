package com.relino.core.register;

import com.relino.core.config.LeaderSelectorConfig;
import com.relino.core.exception.HandleException;
import com.relino.core.support.Utils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于CuratorLeader的Leader节点选取
 *
 * 参考 elasticjob ZookeeperElectionService 重新实现
 *
 * @author kaiqiang.he
 */
public class CuratorLeaderElection {

    private static final Logger log = LoggerFactory.getLogger(CuratorLeaderElection.class);

    private String appId;
    private List<RelinoLeaderElectionListener> leaderSelectorListener;
    private CuratorFramework curatorClient;
    private Map<LeaderSelector, RelinoLeaderElectionListener> selectorElectionMap = new HashMap<>();

    public CuratorLeaderElection(String appId, List<RelinoLeaderElectionListener> leaderSelectorListener, CuratorFramework curatorClient) {

        Utils.checkNonEmpty(appId);
        Utils.checkNoNull(leaderSelectorListener);
        Utils.checkNoNull(curatorClient);

        this.appId = appId;
        this.leaderSelectorListener = leaderSelectorListener;
        this.curatorClient = curatorClient;
    }

    public void execute() {
        if(leaderSelectorListener.isEmpty()) {
            log.info("no leader selections.");
        }

        for (RelinoLeaderElectionListener listener : leaderSelectorListener) {
            LeaderSelectorConfig config = listener.getLeaderSelectorConfig();
            String zkPath = generateZKPath(appId, listener.getLeaderSelectorConfig().getLeaderId());
            LeaderSelector leaderSelector = new LeaderSelector(curatorClient, zkPath, listener);
            selectorElectionMap.put(leaderSelector, listener);
            leaderSelector.autoRequeue();
            leaderSelector.start();
            log.info("{} leader selection start.", config.getLeaderId());
        }
    }

    public void shutdown() {
        log.info("开始结束CuratorLeaderElection");
        selectorElectionMap.forEach((s, l) -> {
            try {
                s.close();
            } catch (Throwable t) {
                HandleException.handleUnExpectedException(t);
            }
        });
        log.info("结束CuratorLeaderElection完成");
    }

    private String generateZKPath(String appId, String leaderId) {
        return "/relino/" + appId + "/leader/" + leaderId;
    }
}
