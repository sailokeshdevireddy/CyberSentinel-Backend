package com.cybersentinel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Machine-readable error codes sent as {@code error.code}.
 * Only codes needed by Phase 1 exist; authentication/authorization codes arrive with the auth phase.
 */
public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "The request could not be processed"),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not allowed for this endpoint"),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type"),
    CONFLICT(HttpStatus.CONFLICT, "Request conflicts with the current state"),
    EMAIL_TAKEN(HttpStatus.CONFLICT, "Email is already taken"),
    SLUG_TAKEN(HttpStatus.CONFLICT, "Slug is already taken"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid credentials"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Account disabled"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    /** Maps framework-raised HTTP statuses (e.g. 405, 415) onto our codes. */
    public static ErrorCode fromStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 400 -> VALIDATION_ERROR;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 409 -> CONFLICT;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            case 503 -> SERVICE_UNAVAILABLE;
            default -> status.is4xxClientError() ? BAD_REQUEST : INTERNAL_ERROR;
        };
    }
}
