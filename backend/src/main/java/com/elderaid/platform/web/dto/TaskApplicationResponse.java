package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.task.ApplicationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskApplicationResponse(
        UUID id,
        UUID taskRequestId,
        UUID workerProfileId,
        String workerFirstName,
        String workerLastName,
        BigDecimal workerAverageRating,
        Integer workerReviewCount,
        ApplicationStatus status,
        String message,
        OffsetDateTime appliedAt
) {
}
