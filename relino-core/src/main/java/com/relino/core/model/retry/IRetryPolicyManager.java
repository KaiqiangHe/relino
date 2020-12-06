package com.relino.core.model.retry;

import com.relino.core.exception.RelinoException;
import com.relino.core.support.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author kaiqiang.he
 */
public class IRetryPolicyManager {

    private static Map<String, IRetryPolicy> retryIdHolders = new HashMap<>();

    private static Map<IRetryPolicy, String> retryHolders = new HashMap<>();

    private IRetryPolicyManager() { }

    public static final String DEFAULT_RETRY_POLICY = "_default";
    public static final String IMMEDIATELY_RETRY_POLICY = "_immediate";
    static {
        register(DEFAULT_RETRY_POLICY, new DefaultIRetryPolicy());
        register(IMMEDIATELY_RETRY_POLICY, new ImmediatelyRetryPolicy());
    }

    public static IRetryPolicy getDefault() {
        return retryIdHolders.get(DEFAULT_RETRY_POLICY);
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
