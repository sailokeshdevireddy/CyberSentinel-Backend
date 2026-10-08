package com.cybersentinel.dto.auth;

import com.cybersentinel.model.enums.UserRole;
import com.cybersentinel.model.enums.UserStatus;
import java.time.Instant;
import java.util.UUID;

public record UserDto(
    UUID id,
    UUID organizationId,
    String name,
    String email,
    UserRole role,
    UserStatus status,
    Instant createdAt,
    Instant lastLoginAt
) {}
