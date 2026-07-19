package com.elderaid.platform.service;

import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.worker.DocumentStatus;
import com.elderaid.platform.domain.worker.DocumentType;
import com.elderaid.platform.domain.worker.VerificationDocument;
import com.elderaid.platform.domain.worker.VerificationStatus;
import com.elderaid.platform.domain.worker.WorkerProfile;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.repository.VerificationDocumentRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.AdminVerificationDocumentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AdminVerificationService {

    private final VerificationDocumentRepository verificationDocumentRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final UserRepository userRepository;

    public AdminVerificationService(
            VerificationDocumentRepository verificationDocumentRepository,
            WorkerProfileRepository workerProfileRepository,
            UserRepository userRepository
    ) {
        this.verificationDocumentRepository = verificationDocumentRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminVerificationDocumentResponse> listPending() {
        return verificationDocumentRepository.findByStatusOrderBySubmittedAtAsc(DocumentStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AdminVerificationDocumentResponse approve(UUID adminUserId, UUID documentId) {
        VerificationDocument document = findPendingOrThrow(documentId);
        AppUser admin = userRepository.getReferenceById(adminUserId);

        document.setStatus(DocumentStatus.APPROVED);
        document.setReviewedBy(admin);
        document.setReviewedAt(OffsetDateTime.now());
        document.setRejectionReason(null);
        document = verificationDocumentRepository.save(document);

        recalculateStatus(document.getWorkerProfile());

        return toResponse(document);
    }

    @Transactional
    public AdminVerificationDocumentResponse reject(UUID adminUserId, UUID documentId, String reason) {
        VerificationDocument document = findPendingOrThrow(documentId);
        AppUser admin = userRepository.getReferenceById(adminUserId);

        document.setStatus(DocumentStatus.REJECTED);
        document.setReviewedBy(admin);
        document.setReviewedAt(OffsetDateTime.now());
        document.setRejectionReason(reason);
        document = verificationDocumentRepository.save(document);

        // Rejection never downgrades a tier the worker already earned from
        // other approved documents - it just blocks this specific document
        // from contributing toward a tier until resubmitted and approved.
        return toResponse(document);
    }

    private VerificationDocument findPendingOrThrow(UUID documentId) {
        VerificationDocument document = verificationDocumentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification document not found"));

        if (document.getStatus() != DocumentStatus.PENDING) {
            throw new ForbiddenOperationException("This document has already been reviewed");
        }
        return document;
    }

    /**
     * Verified requires an approved ID card AND an approved selfie - either
     * alone isn't enough to confirm identity. We only ever move a worker up to
     * VERIFIED here, never automatically back down; revoking verification (e.g.
     * a document later found fraudulent) should be a deliberate admin action,
     * not a side effect of approving something else.
     */
    private void recalculateStatus(WorkerProfile workerProfile) {
        boolean idApproved = hasApproved(workerProfile.getId(), DocumentType.ID_CARD);
        boolean selfieApproved = hasApproved(workerProfile.getId(), DocumentType.SELFIE);

        if (idApproved && selfieApproved && workerProfile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            workerProfile.setVerificationStatus(VerificationStatus.VERIFIED);
            workerProfileRepository.save(workerProfile);
        }
    }

    private boolean hasApproved(UUID workerProfileId, DocumentType documentType) {
        return verificationDocumentRepository
                .findByWorkerProfileIdAndDocumentTypeAndStatus(workerProfileId, documentType, DocumentStatus.APPROVED)
                .isPresent();
    }

    private AdminVerificationDocumentResponse toResponse(VerificationDocument document) {
        AppUser user = document.getWorkerProfile().getUser();
        return new AdminVerificationDocumentResponse(
                document.getId(),
                document.getWorkerProfile().getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                document.getDocumentType(),
                document.getStatus(),
                document.getSubmittedAt()
        );
    }
}
