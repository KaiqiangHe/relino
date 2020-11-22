package com.relino.core.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;

import java.util.function.Supplier;

/**
 * 官方文档和建议stateChanged()：
 * https://curator.apache.org/curator-recipes/leader-election.html
 *
 * @author kaiqiang.he
 */
public class RelinoLeaderElectionListener implements LeaderSelectorListener {

    private String name;

    private String leaderPath;

    private ElectionCandidate task;

    private Supplier<ElectionCandidate> taskSupplier;

    public RelinoLeaderElectionListener(String name, String leaderPath, Supplier<ElectionCandidate> taskSupplier) {
        this.name = name;
        this.leaderPath = leaderPath;
        this.taskSupplier = taskSupplier;
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        task = taskSupplier.get();
        task.executeWhenCandidate();
    }

    @Override
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        if(newState == ConnectionState.LOST || newState == ConnectionState.SUSPENDED) {
            task.stopExecute();
        }
    }

    public String getName() {
        return name;
    }

    public String getLeaderPath() {
        return leaderPath;
    }

}
