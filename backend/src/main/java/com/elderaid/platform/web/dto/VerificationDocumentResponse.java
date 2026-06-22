package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.worker.DocumentStatus;
import com.elderaid.platform.domain.worker.DocumentType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VerificationDocumentResponse(
        UUID id,
        DocumentType documentType,
        DocumentStatus status,
        OffsetDateTime submittedAt,
        OffsetDateTime reviewedAt,
        String rejectionReason
) {
}
