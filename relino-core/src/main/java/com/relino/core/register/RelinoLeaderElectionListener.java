package com.relino.core.register;

import com.relino.core.config.LeaderSelectorConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

/**
 * 官方文档和建议stateChanged()：
 * https://curator.apache.org/curator-recipes/leader-election.html
 *
 * @author kaiqiang.he
 */
public class RelinoLeaderElectionListener implements LeaderSelectorListener {

    private LeaderSelectorConfig leaderSelectorConfig;

    private ElectionCandidate task;

    public RelinoLeaderElectionListener(LeaderSelectorConfig config) {
        leaderSelectorConfig = config;
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        task = leaderSelectorConfig.getTaskSupplier().get();
        task.executeWhenCandidate();
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if(newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED) {
            task.stopExecute();
        }
    }

    public LeaderSelectorConfig getLeaderSelectorConfig() {
        return leaderSelectorConfig;
    }
}
