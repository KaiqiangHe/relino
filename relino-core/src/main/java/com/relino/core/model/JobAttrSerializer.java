package com.relino.core.model;

import java.util.Map;

/**
 * @author kaiqiang.he
 */
public interface JobAttrSerializer {

    String asString(Map<String, String> attr);

    Map<String, String> asAttr(String str);
}
