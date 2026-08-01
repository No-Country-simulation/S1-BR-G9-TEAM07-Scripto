package com.scripto.backend.classification.exception;

public class ClassificationUnavailableException extends RuntimeException {
    public ClassificationUnavailableException(String message) {
        super(message);
    }

    public ClassificationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
