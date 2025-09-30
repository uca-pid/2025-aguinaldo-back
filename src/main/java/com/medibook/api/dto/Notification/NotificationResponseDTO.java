package com.medibook.api.dto.Notification;

import com.medibook.api.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private UUID id;
    private NotificationType type;
    private UUID relatedEntityId;
    private String message;
    private boolean isRead;
    private OffsetDateTime createdAt;
}