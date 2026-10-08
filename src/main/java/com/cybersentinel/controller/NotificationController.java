package com.cybersentinel.controller;

import com.cybersentinel.dto.common.ApiResponse;
import com.cybersentinel.dto.domain.NotificationDto;
import com.cybersentinel.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ApiResponse<List<NotificationDto>> getNotifications() {
        return ApiResponse.ok(notificationService.getNotifications());
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationDto> markAsRead(@PathVariable UUID id) {
        return ApiResponse.ok(notificationService.markAsRead(id));
    }

    @PostMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.ok(null);
    }
}
