package com.relino.core.model;


import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class JobAttrTest {

    private static final Logger log = LoggerFactory.getLogger(JobAttrTest.class);

    @Test
    public void testJobAttr() {
        JobAttr jobAttr = new JobAttr();
        String s = "hello";
        Long l = 1L;
        Double d = 1.111;
        Boolean b = false;
        LocalDateTime time = LocalDateTime.now();

        jobAttr.setString("s", s);
        jobAttr.setLong("l", l);
        jobAttr.setDouble("d", d);
        jobAttr.setBoolean("b", b);
        jobAttr.setLocalDateTime("time", time);

        Assert.assertEquals(s, jobAttr.getString("s"));
        Assert.assertEquals(l, jobAttr.getLong("l"));
        Assert.assertEquals(d, jobAttr.getDouble("d"));
        Assert.assertEquals(b, jobAttr.getBoolean("b"));
        Assert.assertEquals(time, jobAttr.getLocalDateTime("time"));
        Assert.assertNull(jobAttr.getString("xxxx"));

        String str = jobAttr.asString();
        log.info("jobAttr = {}", str);
        jobAttr = JobAttr.asObj(str);

        Assert.assertEquals(s, jobAttr.getString("s"));
        Assert.assertEquals(l, jobAttr.getLong("l"));
        Assert.assertEquals(d, jobAttr.getDouble("d"));
        Assert.assertEquals(b, jobAttr.getBoolean("b"));
        Assert.assertEquals(time, jobAttr.getLocalDateTime("time"));
        Assert.assertNull(jobAttr.getString("xxxx"));
    }
}
