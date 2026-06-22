package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.worker.DocumentStatus;
import com.elderaid.platform.domain.worker.DocumentType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminVerificationDocumentResponse(
        UUID id,
        UUID workerProfileId,
        String workerFirstName,
        String workerLastName,
        String workerEmail,
        DocumentType documentType,
        DocumentStatus status,
        OffsetDateTime submittedAt
) {
}
