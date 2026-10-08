package com.cybersentinel.dto.auth;

import com.cybersentinel.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;

public record UpdatePasswordRequest(
    @NotBlank
    String currentPassword,
    
    @ValidPassword
    String newPassword,
    
    @NotBlank
    String confirmPassword
) {}
