package com.relino.core.exception;

/**
 * @author kaiqiang.he
 */
public class RelinoException extends RuntimeException {

    public RelinoException() {
    }

    public RelinoException(String message) {
        super(message);
    }

    public RelinoException(String message, Throwable cause) {
        super(message, cause);
    }
}
