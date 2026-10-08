package com.cybersentinel.dto.common;

import java.time.Instant;

/** Payload of GET /api/health. {@code database} is the schema actually connected to. */
public record HealthResponse(String status, String application, String database, Instant timestamp) {
}
