package com.relino.core.config;

import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.Utils;

import java.util.function.Supplier;

/**
 * @author kaiqiang.he
 */
public class LeaderSelectorConfig {

    private String name;

    private String leaderPath;

    private Supplier<ElectionCandidate> taskSupplier;

    public LeaderSelectorConfig(String name, String leaderPath, Supplier<ElectionCandidate> taskSupplier) {

        Utils.checkNonEmpty(name);
        Utils.checkNonEmpty(leaderPath);
        Utils.checkNoNull(taskSupplier);

        this.name = name;
        this.leaderPath = leaderPath;
        this.taskSupplier = taskSupplier;
    }

    public String getName() {
        return name;
    }

    public String getLeaderPath() {
        return leaderPath;
    }

    public Supplier<ElectionCandidate> getTaskSupplier() {
        return taskSupplier;
    }
}
