package com.cybersentinel.service;

import com.cybersentinel.dto.domain.NotificationDto;
import com.cybersentinel.exception.ApiException;
import com.cybersentinel.model.Notification;
import com.cybersentinel.repository.NotificationRepository;
import com.cybersentinel.security.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TenantContext tenantContext;

    public NotificationService(NotificationRepository notificationRepository, TenantContext tenantContext) {
        this.notificationRepository = notificationRepository;
        this.tenantContext = tenantContext;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        UUID userId = tenantContext.getCurrentUserId();
        return notificationRepository.findUserNotifications(orgId, userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto markAsRead(UUID id) {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        UUID userId = tenantContext.getCurrentUserId();
        Notification notif = notificationRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> ApiException.notFound("Notification not found"));

        if (notif.getUserId() != null && !notif.getUserId().equals(userId)) {
            throw ApiException.notFound("Notification not found");
        }

        notif.setRead(true);
        notif = notificationRepository.save(notif);
        return mapToDto(notif);
    }

    @Transactional
    public void markAllAsRead() {
        UUID orgId = tenantContext.getCurrentOrganizationId();
        UUID userId = tenantContext.getCurrentUserId();
        List<Notification> unread = notificationRepository.findUserNotifications(orgId, userId).stream()
                .filter(n -> !n.getRead())
                .collect(Collectors.toList());

        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private NotificationDto mapToDto(Notification notif) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notif.getId());
        dto.setOrganizationId(notif.getOrganizationId());
        dto.setUserId(notif.getUserId());
        dto.setType(notif.getType());
        dto.setTitle(notif.getTitle());
        dto.setMessage(notif.getMessage());
        dto.setRelatedEntityType(notif.getRelatedEntityType());
        dto.setRelatedEntityId(notif.getRelatedEntityId());
        dto.setRead(notif.getRead());
        dto.setCreatedAt(notif.getCreatedAt());
        return dto;
    }
}
