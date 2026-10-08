# Tenant Isolation Strategy (Phase 4)

This document outlines the tenant isolation strategies applied to the CyberSentinel backend.

## Tenant Identity Source
- Tenant identity comes strictly from the `AuthenticatedUser` retrieved from the Spring `SecurityContext`.
- The `TenantContext` component exposes the current `organizationId`, `userId`, and `role`.
- `TenantContext` operates on a strict **fail-closed** policy: if no authenticated tenant is present, it explicitly throws an `IllegalStateException`.

## Repository Scoping
- All entities with an `organizationId` are scoped at the repository level.
- Global lookups (e.g., `findAll()`, `findById()`) are avoided for tenant-specific data.
- Domain services use methods like `findByOrganizationId(orgId)` and `findByIdAndOrganizationId(id, orgId)` exclusively.

## Child Entity Scoping
- Entities like `IncidentNote` and `IncidentTimelineEntry` do not contain an `organizationId` column (avoiding unnecessary schema changes).
- These entities are scoped via their parent.
- Before accessing child entities, services invoke `TenantGuard.verifyIncidentAccess(incidentId)`.
- `RiskAssessment` remains scoped through its EVENT/INCIDENT subject.

## Notification Scoping
- A user can access notifications only if the `organizationId` matches AND:
  - The notification's `userId` matches the current user's ID, OR
  - The notification's `userId` is `null` (representing an organization-wide broadcast).

## 404 Cross-Tenant Behavior
- Cross-tenant access attempts MUST explicitly resolve as `404 NOT_FOUND` rather than `403 FORBIDDEN` or `401 UNAUTHORIZED`.
- This is enforced by `TenantGuard`, which throws an `ApiException.notFound()` when parent entities are not found within the current tenant's scope.
- This obscures the existence of resources across tenants and prevents enumeration attacks.

## Current-Role Behavior
- The current available roles are strictly `ORG_ADMIN` and `ANALYST`.
- **There is NO SUPER_ADMIN role.**
- No cross-tenant bypass exists in the system. Tenant isolation applies indiscriminately to all current roles.
