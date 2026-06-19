package com.elderaid.platform.exception;

public class InvalidRegistrationRoleException extends RuntimeException {
    public InvalidRegistrationRoleException() {
        super("This role cannot be selected during self-registration");
    }
}
