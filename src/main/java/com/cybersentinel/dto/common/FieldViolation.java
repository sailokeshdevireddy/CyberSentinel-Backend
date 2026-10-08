package com.cybersentinel.dto.common;

/** One entry of {@code error.details} for VALIDATION_ERROR responses. */
public record FieldViolation(String field, String message) {
}
