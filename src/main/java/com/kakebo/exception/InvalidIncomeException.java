package com.kakebo.exception;

public class InvalidIncomeException extends RuntimeException {
    public InvalidIncomeException(String message) {
        super(message);
    }

    public InvalidIncomeException(String message, Throwable cause) {
        super(message, cause);
    }
}
