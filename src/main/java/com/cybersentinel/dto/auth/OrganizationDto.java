package com.cybersentinel.dto.auth;

import com.cybersentinel.model.enums.OrganizationStatus;
import java.util.UUID;

public record OrganizationDto(
    UUID id,
    String name,
    String slug,
    OrganizationStatus status
) {}
