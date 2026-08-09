package com.scripto.backend.exception;

public class AccountPendingReactivationException extends RuntimeException {
    public AccountPendingReactivationException(String message) {
        super(message);
    }
}
