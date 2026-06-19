package com.elderaid.platform.web.dto;

import jakarta.validation.constraints.NotNull;

public record RefreshRequest(
        @NotNull
        String refreshToken
) {
}
