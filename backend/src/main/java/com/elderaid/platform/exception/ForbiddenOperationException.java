package com.elderaid.platform.exception;

public class ForbiddenOperationException extends RuntimeException {

    private final String errorCode;

    public ForbiddenOperationException(String message) {
        super(message);
        this.errorCode = "FORBIDDEN_OPERATION";
    }

    public ForbiddenOperationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
