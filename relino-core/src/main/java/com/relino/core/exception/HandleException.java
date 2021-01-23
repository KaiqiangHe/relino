package com.relino.core.exception;

import com.relino.core.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author kaiqiang.he
 */
public class HandleException {

    private static final Logger log = LoggerFactory.getLogger(HandleException.class);

    /**
     * 处理不预期的异常
     *
     * @param t not null
     */
    public static void handleUnExpectedException(Throwable t) {
        handleUnExpectedException(t, null);
    }

    public static void handleThreadInterruptedException(InterruptedException e) {
        log.error("InterruptedException error occur ", e);
    }

    /**
     * 处理不预期的异常
     */
    public static void handleUnExpectedException(Throwable t, String message) {
        Utils.checkNoNull(t);
        if(Utils.isEmpty(message)) {
            log.error("Unexpected error occur ", t);
        } else {
            log.error("Unexpected error occur {}", message, t);
        }
    }

}
