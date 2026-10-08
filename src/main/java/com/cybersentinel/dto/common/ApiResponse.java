package com.cybersentinel.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Locked response envelope.
 * Success: {"success":true,"data":...,"meta":...}   (meta omitted unless provided)
 * Failure: {"success":false,"error":{"code":...,"message":...,"details":...}}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, PageMeta meta, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, PageMeta meta) {
        return new ApiResponse<>(true, data, meta, null);
    }

    public static ApiResponse<Void> failure(ApiError error) {
        return new ApiResponse<>(false, null, null, error);
    }
}
