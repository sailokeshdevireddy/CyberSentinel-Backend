package com.cybersentinel.controller;

import com.cybersentinel.dto.auth.LoginRequest;
import com.cybersentinel.dto.auth.LoginResponse;
import com.cybersentinel.dto.auth.RegisterRequest;
import com.cybersentinel.dto.auth.RegisterResponse;
import com.cybersentinel.dto.auth.UpdateProfileRequest;
import com.cybersentinel.dto.auth.UserDto;
import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.security.AuthenticatedUser;
import com.cybersentinel.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ApiResponse.ok(response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.ok(response);
    }

    @GetMapping("/me")
    public ApiResponse<UserDto> getMe(@AuthenticationPrincipal AuthenticatedUser authUser) {
        UserDto response = authService.getMe(authUser);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/me")
    public ApiResponse<UserDto> updateMe(@AuthenticationPrincipal AuthenticatedUser authUser,
                                         @Valid @RequestBody UpdateProfileRequest request) {
        UserDto response = authService.updateMe(authUser, request);
        return ApiResponse.ok(response);
    }

    @PatchMapping("/password")
    public ApiResponse<Void> updatePassword(@AuthenticationPrincipal AuthenticatedUser authUser,
                                            @Valid @RequestBody com.cybersentinel.dto.auth.UpdatePasswordRequest request) {
        authService.updatePassword(authUser, request);
        return ApiResponse.ok(null);
    }
}
