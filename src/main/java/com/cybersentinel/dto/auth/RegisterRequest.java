package com.cybersentinel.dto.auth;

import com.cybersentinel.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank
    String organizationName,
    
    @NotBlank
    String name,
    
    @NotBlank
    @Email
    String email,
    
    @ValidPassword
    String password
) {}
