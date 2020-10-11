package com.kaiqiang.relino.core.ops;

import lombok.Data;

@Data
public class Oper {

    /**
     * 执行的动作
     */
    private Execute execute;

    public Oper(Execute execute) {
        this.execute = execute;
    }
}
