package com.elderaid.platform.web.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID bookingId,
        UUID ratedUserId,
        Integer rating,
        String comment,
        OffsetDateTime createdAt
) {
}
