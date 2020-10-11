package com.kaiqiang.relino.core.temp;

import com.kaiqiang.relino.core.ops.Execute;
import com.kaiqiang.relino.core.ops.Result;

public class PayFailedJustLogExecute implements Execute {

    @Override
    public Result execute(int executeCount) {
        System.out.println("pay failed just log");
        return new Result();
    }
}
