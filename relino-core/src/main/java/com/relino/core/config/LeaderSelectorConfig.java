package com.relino.core.config;

import com.relino.core.register.ElectionCandidate;
import com.relino.core.support.Utils;

import java.util.function.Supplier;

/**
 * @author kaiqiang.he
 */
public class LeaderSelectorConfig {

    private String leaderId;

    private Supplier<ElectionCandidate> taskSupplier;

    public LeaderSelectorConfig(String leaderId, Supplier<ElectionCandidate> taskSupplier) {

        Utils.checkNonEmpty(leaderId);
        Utils.checkNoNull(taskSupplier);

        this.leaderId = leaderId;

        this.taskSupplier = taskSupplier;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public Supplier<ElectionCandidate> getTaskSupplier() {
        return taskSupplier;
    }
}
