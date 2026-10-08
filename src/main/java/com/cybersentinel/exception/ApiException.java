package com.cybersentinel.exception;

/** Throw from services/controllers to produce a specific error envelope. */
public class ApiException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object details;

    public ApiException(ErrorCode errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public static ApiException validation(String message, Object details) {
        return new ApiException(ErrorCode.VALIDATION_ERROR, message, details);
    }

    public static ApiException notFound(String message) {
        return new ApiException(ErrorCode.NOT_FOUND, message);
    }

    public static ApiException conflict(String message) {
        return new ApiException(ErrorCode.CONFLICT, message);
    }

    public static ApiException conflict(String code, String message) {
        return new ApiException(ErrorCode.valueOf(code), message);
    }

    public static ApiException unauthorized(String code, String message) {
        return new ApiException(ErrorCode.valueOf(code), message);
    }

    public static ApiException forbidden(String code, String message) {
        return new ApiException(ErrorCode.valueOf(code), message);
    }

    public static ApiException internalError(String message) {
        return new ApiException(ErrorCode.INTERNAL_ERROR, message);
    }

    public static ApiException serviceUnavailable(String message) {
        return new ApiException(ErrorCode.SERVICE_UNAVAILABLE, message);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object getDetails() {
        return details;
    }
}
