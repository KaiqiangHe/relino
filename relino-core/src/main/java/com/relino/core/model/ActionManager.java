package com.relino.core.model;

import com.relino.core.exception.RelinoException;
import com.relino.core.support.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author kaiqiang.he
 */
public class ActionManager {

    private static Map<String, Action> actionHolders = new HashMap<>();

    public ActionManager() { }

    /**
     * 注册一个action
     *
     * @param actionId not empty
     * @param action not null
     */
    public static void register(String actionId, Action action) {
        Utils.checkNonEmpty(actionId);
        Objects.requireNonNull(action);

        if(containsAction(actionId)) {
            throw new RelinoException("actionId = " + actionId + "不存在");
        }

        actionHolders.put(actionId, action);
    }

    public static boolean containsAction(String key) {
        return actionHolders.containsKey(key);
    }

    /**
     * @return nullable
     */
    public static Action getAction(String key) {
        return actionHolders.get(key);
    }
}
