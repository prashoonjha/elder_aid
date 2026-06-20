package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.task.TaskStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskSummaryResponse(
        UUID id,
        TaskCategory category,
        String description,
        String city,
        OffsetDateTime scheduledStart,
        OffsetDateTime scheduledEnd,
        BigDecimal priceOffered,
        TaskStatus status
) {
}
