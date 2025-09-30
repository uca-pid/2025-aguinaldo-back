package com.medibook.api.controller;

import com.medibook.api.dto.Notification.NotificationResponseDTO;
import com.medibook.api.entity.Notification;
import com.medibook.api.entity.User;
import com.medibook.api.service.NotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Object> getNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            HttpServletRequest request) {

        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

        List<Notification> notifications;
        if (unreadOnly) {
            notifications = notificationService.getUnreadNotifications(authenticatedUser.getId());
        } else {
            notifications = notificationService.getAllNotifications(authenticatedUser.getId());
        }

        List<NotificationResponseDTO> dtos = notifications.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("notifications", dtos));
    }

    @GetMapping("/count")
    public ResponseEntity<Object> getUnreadCount(HttpServletRequest request) {
        User authenticatedUser = (User) request.getAttribute("authenticatedUser");
        long count = notificationService.getUnreadCount(authenticatedUser.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Object> markAsRead(
            @PathVariable UUID notificationId,
            HttpServletRequest request) {

        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

        boolean success = notificationService.markAsRead(notificationId, authenticatedUser.getId());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } else {
            return new ResponseEntity<>(
                Map.of("error", "Not Found", "message", "Notification not found or access denied"),
                HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Object> deleteNotification(
            @PathVariable UUID notificationId,
            HttpServletRequest request) {

        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

        boolean success = notificationService.deleteNotification(notificationId, authenticatedUser.getId());
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Notification deleted"));
        } else {
            return new ResponseEntity<>(
                Map.of("error", "Not Found", "message", "Notification not found or access denied"),
                HttpStatus.NOT_FOUND);
        }
    }

    private NotificationResponseDTO mapToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .type(notification.getType())
                .relatedEntityId(notification.getRelatedEntityId())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}