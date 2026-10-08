package com.cybersentinel.dto.common;

/** Pagination block for {@code meta}. Only paginated endpoints (e.g. events) use it. */
public record PageMeta(int page, int pageSize, long totalItems, int totalPages) {

    public static PageMeta of(int page, int pageSize, long totalItems) {
        int totalPages = pageSize <= 0 ? 0 : (int) Math.ceil((double) totalItems / pageSize);
        return new PageMeta(page, pageSize, totalItems, totalPages);
    }
}
