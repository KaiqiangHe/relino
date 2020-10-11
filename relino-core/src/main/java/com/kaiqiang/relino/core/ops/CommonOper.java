package com.kaiqiang.relino.core.ops;

import lombok.Data;

/**
 * 至多执行一次
 */
@Data
public class CommonOper extends Oper {

    public CommonOper(Execute execute) {
        super(execute);
    }
}
