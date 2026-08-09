package com.scripto.backend.exception;

public class AccountDeletionExpiredException extends RuntimeException {
    public AccountDeletionExpiredException(String message) {
        super(message);
    }
}
