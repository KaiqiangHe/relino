package com.relino.core.register;

import com.relino.core.config.LeaderSelectorConfig;
import com.relino.core.exception.HandleException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

/**
 * 官方文档和建议stateChanged()：
 * https://curator.apache.org/curator-recipes/leader-election.html
 *
 * @author kaiqiang.he
 */
public class RelinoLeaderElectionListener extends LeaderSelectorListenerAdapter {

    private LeaderSelectorConfig leaderSelectorConfig;

    private ElectionCandidate task;

    public RelinoLeaderElectionListener(LeaderSelectorConfig config) {
        leaderSelectorConfig = config;
    }

    @Override
    public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
        task = leaderSelectorConfig.getTaskSupplier().get();
        try {
            task.executeWhenCandidate();
        } catch (InterruptedException e) {
            // ignore
            HandleException.handleThreadInterruptedException(e);
        } catch (Exception e) {
            HandleException.handleUnExpectedException(e);
        } finally {
            task.destroy();
        }
    }

    public LeaderSelectorConfig getLeaderSelectorConfig() {
        return leaderSelectorConfig;
    }

    public ElectionCandidate getTask() {
        return task;
    }
}
