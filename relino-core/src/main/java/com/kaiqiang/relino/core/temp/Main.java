package com.kaiqiang.relino.core.temp;

import com.kaiqiang.relino.core.ops.CommonOper;
import com.kaiqiang.relino.core.ops.Job;
import com.kaiqiang.relino.core.ops.ReliableOper;

import java.time.LocalDateTime;

/**
 *
 */
public class Main {

    public static void main(String[] args) {

        ReliableOper mainOper = new ReliableOper(new PayExecute(), 10);
        ReliableOper onSuccess = new ReliableOper(new PayCallbackNoticeExecute(), 10);
        CommonOper onFailed = new CommonOper(new PayFailedJustLogExecute());

        Job payJob = new Job();
        payJob.setJobId("mock-id" + System.currentTimeMillis());
        payJob.setBeginTime(LocalDateTime.now().plusMinutes(10));
        payJob.setMainOper(mainOper);
        payJob.setOnSuccess(onSuccess);
        payJob.setOnFailed(onFailed);

        payJob.execute();
    }

}