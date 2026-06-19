package com.elderaid.platform.exception;

public class EmailAlreadyInUseException extends RuntimeException {
    public EmailAlreadyInUseException(String email) {
        super("An account with email " + email + " already exists");
    }
}
