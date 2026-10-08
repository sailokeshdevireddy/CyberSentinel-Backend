package com.cybersentinel.dto.common;

/**
 * Error body inside the failure envelope. The frontend switches on {@code code}, never on {@code message}.
 * {@code details} is serialized as an explicit null when absent, matching the locked contract.
 */
public record ApiError(String code, String message, Object details) {
}
