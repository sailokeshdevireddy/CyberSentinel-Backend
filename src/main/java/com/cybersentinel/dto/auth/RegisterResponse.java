package com.cybersentinel.dto.auth;

public record RegisterResponse(
    AuthUserDto user,
    OrganizationDto organization
) {}
