package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.task.TaskStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskDetailResponse(
        UUID id,
        UUID elderlyProfileId,
        TaskCategory category,
        String description,
        Double locationLat,
        Double locationLng,
        String addressLine,
        String city,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        BigDecimal priceOffered,
        TaskStatus status,
        OffsetDateTime createdAt
) {
}
