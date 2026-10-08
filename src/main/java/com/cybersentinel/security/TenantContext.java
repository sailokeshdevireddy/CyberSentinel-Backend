package com.cybersentinel.security;

import com.cybersentinel.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TenantContext {

    public AuthenticatedUser getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth instanceof AuthenticatedUser)) {
            throw new IllegalStateException("No authenticated tenant context found");
        }
        return (AuthenticatedUser) auth;
    }

    public UUID getCurrentOrganizationId() {
        return getAuthenticatedUser().getOrganizationId();
    }

    public UUID getCurrentUserId() {
        return getAuthenticatedUser().getUserId();
    }

    public UserRole getCurrentRole() {
        return getAuthenticatedUser().getRole();
    }
}
