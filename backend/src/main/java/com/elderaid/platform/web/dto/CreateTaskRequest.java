package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.task.TaskCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTaskRequest(

        @NotNull
        UUID elderlyProfileId,

        @NotNull
        TaskCategory category,

        String description,

        Double locationLat,
        Double locationLng,
        String addressLine,
        String city,

        @NotNull
        OffsetDateTime scheduledStart,

        @NotNull
        OffsetDateTime scheduledEnd,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal priceOffered
) {
}
