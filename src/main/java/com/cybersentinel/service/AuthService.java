package com.cybersentinel.service;

import com.cybersentinel.dto.auth.*;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Organization;
import com.cybersentinel.model.User;
import com.cybersentinel.model.enums.OrganizationStatus;
import com.cybersentinel.model.enums.UserRole;
import com.cybersentinel.model.enums.UserStatus;
import com.cybersentinel.repository.OrganizationRepository;
import com.cybersentinel.repository.UserRepository;
import com.cybersentinel.security.AuthenticatedUser;
import com.cybersentinel.security.JwtService;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final JwtService jwtService;
    private final Clock clock;
    private final PasswordEncoder passwordEncoder;
    private final String dummyPasswordHash;

    public AuthService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       JwtService jwtService,
                       Clock clock) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.jwtService = jwtService;
        this.clock = clock;
        this.passwordEncoder = new BCryptPasswordEncoder();
        // Dummy hash to prevent timing attacks, using the same BCrypt strength
        this.dummyPasswordHash = this.passwordEncoder.encode("dummyPassword123!");
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("EMAIL_TAKEN", "Email is already taken");
        }

        String baseSlug = slugify(request.organizationName());
        String finalSlug = baseSlug;
        int suffix = 2;
        boolean slugSaved = false;
        Organization organization = new Organization();
        
        while (!slugSaved) {
            if (organizationRepository.existsBySlug(finalSlug)) {
                finalSlug = baseSlug + "-" + suffix;
                suffix++;
                if (suffix > 100) throw ApiException.conflict("SLUG_TAKEN", "Unable to generate unique slug");
            } else {
                organization.setName(request.organizationName().trim());
                organization.setSlug(finalSlug);
                organization.setStatus(OrganizationStatus.ACTIVE);
                organization = organizationRepository.saveAndFlush(organization);
                slugSaved = true;
            }
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(UserRole.ORG_ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        user.setOrganizationId(organization.getId());

        try {
            user = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("EMAIL_TAKEN", "Email is already taken");
        }

        AuthUserDto authUserDto = new AuthUserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganizationId()
        );

        OrganizationDto orgDto = new OrganizationDto(
                organization.getId(),
                organization.getName(),
                organization.getSlug(),
                organization.getStatus()
        );

        return new RegisterResponse(authUserDto, orgDto);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        Optional<User> optionalUser = userRepository.findByEmailIgnoreCase(email);

        boolean passwordMatches;
        if (optionalUser.isPresent()) {
            passwordMatches = passwordEncoder.matches(request.password(), optionalUser.get().getPasswordHash());
        } else {
            // Dummy check to mitigate timing attacks
            passwordEncoder.matches(request.password(), dummyPasswordHash);
            passwordMatches = false;
        }

        if (!passwordMatches) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid credentials");
        }

        User user = optionalUser.get();

        Organization organization = organizationRepository.findById(user.getOrganizationId())
                .orElseThrow(() -> ApiException.internalError("Organization not found"));

        if (user.getStatus() == UserStatus.DISABLED || organization.getStatus() == OrganizationStatus.SUSPENDED) {
            throw ApiException.forbidden("ACCOUNT_DISABLED", "Account is disabled");
        }

        user.setLastLoginAt(Instant.now(clock));
        userRepository.save(user);

        String token = jwtService.generateToken(user.getId(), organization.getId(), user.getRole().name());
        Instant expiresAt = Instant.now(clock).plusMillis(jwtService.getExpirationMs());

        AuthUserDto authUserDto = new AuthUserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getOrganizationId()
        );

        return new LoginResponse(token, expiresAt, authUserDto);
    }

    public UserDto getMe(AuthenticatedUser authUser) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> ApiException.internalError("User not found"));

        return mapToUserDto(user);
    }

    @Transactional
    public UserDto updateMe(AuthenticatedUser authUser, UpdateProfileRequest request) {
        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> ApiException.internalError("User not found"));

        user.setName(request.name().trim());
        user = userRepository.save(user);

        return mapToUserDto(user);
    }

    @Transactional
    public void updatePassword(AuthenticatedUser authUser, UpdatePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw ApiException.validation("Passwords do not match", null);
        }

        User user = userRepository.findById(authUser.getUserId())
                .orElseThrow(() -> ApiException.internalError("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private UserDto mapToUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getOrganizationId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getLastLoginAt()
        );
    }

    private String slugify(String input) {
        if (input == null) return "org";
        String slug = input.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        slug = slug.replaceAll("^-+|-+$", "");
        if (slug.isEmpty()) {
            slug = "org";
        }
        if (slug.length() > 50) { // assuming standard length limits
            slug = slug.substring(0, 50);
            slug = slug.replaceAll("-+$", "");
        }
        return slug;
    }
}
