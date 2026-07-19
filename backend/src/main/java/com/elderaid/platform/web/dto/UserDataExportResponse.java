package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.elderly.PermissionLevel;
import com.elderaid.platform.domain.task.ApplicationStatus;
import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.task.TaskStatus;
import com.elderaid.platform.domain.worker.DocumentStatus;
import com.elderaid.platform.domain.worker.DocumentType;
import com.elderaid.platform.domain.worker.VerificationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Everything personally tied to one account, in one bundle. Deliberately
 * does not include other people's data even where it's related (e.g. who
 * applied to a task this user posted) - this export is about what we hold
 * on the requester, not a window into other users' records.
 */
public record UserDataExportResponse(
        Account account,
        List<Consent> consents,
        List<ElderlyProfileSummary> elderlyProfilesManaged,
        List<PostedTask> tasksPosted,
        WorkerProfileSummary workerProfile
) {

    public record Account(
            UUID id,
            String email,
            String firstName,
            String lastName,
            String phone,
            String locale,
            OffsetDateTime createdAt
    ) {
    }

    public record Consent(
            String consentType,
            boolean given,
            String policyVersion,
            OffsetDateTime createdAt
    ) {
    }

    public record ElderlyProfileSummary(
            UUID id,
            String firstName,
            String lastName,
            String relationship,
            PermissionLevel permissionLevel
    ) {
    }

    public record PostedTask(
            UUID id,
            TaskCategory category,
            TaskStatus status,
            BigDecimal priceOffered,
            OffsetDateTime createdAt
    ) {
    }

    public record WorkerProfileSummary(
            VerificationStatus verificationStatus,
            BigDecimal averageRating,
            Integer completedTasksCount,
            List<VerificationDocumentSummary> documents,
            List<ApplicationSummary> applications,
            List<BookingSummary> bookings
    ) {
    }

    public record VerificationDocumentSummary(
            DocumentType documentType,
            DocumentStatus status,
            OffsetDateTime submittedAt
    ) {
    }

    public record ApplicationSummary(
            UUID taskRequestId,
            ApplicationStatus status,
            OffsetDateTime appliedAt
    ) {
    }

    public record BookingSummary(
            UUID id,
            UUID taskRequestId,
            String status,
            OffsetDateTime checkInTime,
            OffsetDateTime checkOutTime
    ) {
    }
}
