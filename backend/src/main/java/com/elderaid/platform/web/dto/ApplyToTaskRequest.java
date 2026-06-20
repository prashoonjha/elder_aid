package com.elderaid.platform.web.dto;

import jakarta.validation.constraints.Size;

public record ApplyToTaskRequest(
        @Size(max = 1000)
        String message
) {
}
