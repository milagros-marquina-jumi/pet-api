package com.marquina.pet_api.exception;

public class ExternalApiTimeoutException extends RuntimeException {

    public ExternalApiTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
