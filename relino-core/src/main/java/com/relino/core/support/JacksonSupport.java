package com.relino.core.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class JacksonSupport {

    public static final ObjectMapper defaultMapper;
    static {
        defaultMapper = new ObjectMapper();
        defaultMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        defaultMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        // 时间
        SimpleDateFormat smt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        defaultMapper.setDateFormat(smt);
        defaultMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        defaultMapper.registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());


    }

    public static String toJson(Object obj) {
        try {
            return defaultMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parse(String jsonStr, Class<T> clazz) {
        try {
            return defaultMapper.readValue(jsonStr, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
