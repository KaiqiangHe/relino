package com.relino.core.support.id;

import java.util.UUID;

/**
 * 基于uuid的IdGenerator
 *
 * @author kaiqiang.he
 */
public class UUIDIdGenerator implements IdGenerator {

    @Override
    public String getNext() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
