package com.relino.core.db;

import com.relino.core.model.Job;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author kaiqiang.he
 */
public class DBBasedStore implements Store {

    @Override
    public void insertJob(Job job) {

    }

    @Override
    public void updateJob(Job job, boolean updateCommonAttr) {

    }

    @Override
    public List<Long> getRunnableDelayJobId(LocalDateTime start, LocalDateTime end, int limit) {
        return null;
    }

    @Override
    public void setDelayJobRunnable(List<IdAndExecuteOrder> updateData) {

    }
}
