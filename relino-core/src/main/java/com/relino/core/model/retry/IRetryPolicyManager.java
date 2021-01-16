package com.relino.core.model.retry;

import com.relino.core.exception.RelinoException;
import com.relino.core.support.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * // TODO: 2021/1/16  改为非静态方法
 *
 * @author kaiqiang.he
 */
public class IRetryPolicyManager {

    private static Map<String, IRetryPolicy> retryIdHolders = new HashMap<>();

    private static Map<IRetryPolicy, String> retryHolders = new HashMap<>();

    private IRetryPolicyManager() { }

    public static final String DEFAULT_RETRY_POLICY = "_df";
    public static final String IMMEDIATELY_RETRY_POLICY = "_im";
    static {
        register(IMMEDIATELY_RETRY_POLICY, new ImmediatelyRetryPolicy());
    }

    public static IRetryPolicy getDefault() {
        return retryIdHolders.get(DEFAULT_RETRY_POLICY);
    }

    public static void registerDefault(IRetryPolicy retry) {
        register(DEFAULT_RETRY_POLICY, retry);
    }

    /**
     * 注册一个action
     *
     * @param retryId not empty
     * @param retry not null
     */
    public static void register(String retryId, IRetryPolicy retry) {
        Utils.checkNonEmpty(retryId);
        Objects.requireNonNull(retry);

        if(containsIRetryAfter(retryId)) {
            throw new RelinoException("retryId=" + retryId + "已经注册");
        }

        if(retryHolders.containsKey(retry)) {
            throw new RelinoException("IRetryPolicy只能注册一次");
        }

        retryIdHolders.put(retryId, retry);
        retryHolders.put(retry, retryId);
    }

    public static boolean containsIRetryAfter(String retryId) {
        return retryIdHolders.containsKey(retryId);
    }

    /**
     * @return nullable
     */
    public static String getRetryId(IRetryPolicy retry) {
        return retryHolders.get(retry);
    }

    /**
     * @return nullable
     */
    public static IRetryPolicy getIRetryAfter(String retryId) {
        return retryIdHolders.get(retryId);
    }
}
