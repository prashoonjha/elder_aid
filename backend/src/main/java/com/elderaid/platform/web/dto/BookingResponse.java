package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.booking.BookingStatus;
import com.elderaid.platform.domain.task.TaskCategory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID taskRequestId,
        UUID workerProfileId,
        BookingStatus status,
        OffsetDateTime checkInTime,
        OffsetDateTime checkOutTime,
        OffsetDateTime createdAt,
        TaskCategory taskCategory,
        String taskCity,
        OffsetDateTime taskScheduledStart,
        BigDecimal taskPriceOffered
) {
}
