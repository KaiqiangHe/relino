package com.relino.core.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.relino.core.model.JobAttrSerializer;

import java.util.Map;

/**
 * @author kaiqiang.he
 */
public class JacksonJobAttrSerializer implements JobAttrSerializer {

    @Override
    public String asString(Map<String, String> attr) {
        return JacksonSupport.toJson(attr);
    }

    @Override
    public Map<String, String> asAttr(String str) {
        try {
            return JacksonSupport.defaultMapper.readValue(str, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
