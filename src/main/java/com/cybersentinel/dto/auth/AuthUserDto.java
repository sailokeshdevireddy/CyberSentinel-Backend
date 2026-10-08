package com.cybersentinel.dto.auth;

import com.cybersentinel.model.enums.UserRole;
import java.util.UUID;

public record AuthUserDto(
    UUID id,
    String name,
    String email,
    UserRole role,
    UUID organizationId
) {}
