package com.cybersentinel.repository;

import com.cybersentinel.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query("SELECT n FROM Notification n WHERE n.organizationId = :orgId AND (n.userId IS NULL OR n.userId = :userId)")
    List<Notification> findUserNotifications(@Param("orgId") UUID orgId, @Param("userId") UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.organizationId = :orgId AND (n.userId IS NULL OR n.userId = :userId)")
    Optional<Notification> findByIdAndUserAccess(@Param("id") UUID id, @Param("orgId") UUID orgId, @Param("userId") UUID userId);

    Optional<Notification> findByIdAndOrganizationId(UUID id, UUID organizationId);
}
