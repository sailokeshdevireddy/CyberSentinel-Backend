package com.cybersentinel.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record UpdateProfileRequest(
    @NotBlank
    String name
) {}
