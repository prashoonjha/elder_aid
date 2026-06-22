package com.elderaid.platform.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectDocumentRequest(
        @NotBlank
        String reason
) {
}
