package com.elderaid.platform.service;

import com.elderaid.platform.domain.worker.DocumentStatus;
import com.elderaid.platform.domain.worker.DocumentType;
import com.elderaid.platform.domain.worker.VerificationDocument;
import com.elderaid.platform.domain.worker.WorkerProfile;
import com.elderaid.platform.exception.InvalidRequestException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.VerificationDocumentRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.storage.FileStorageService;
import com.elderaid.platform.web.dto.VerificationDocumentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkerVerificationService {

    // Photos of an ID card and a selfie, or a PDF for the criminal record
    // extract - nothing else has a legitimate reason to land here.
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "application/pdf"
    );
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024;

    private final VerificationDocumentRepository verificationDocumentRepository;
    private final WorkerProfileRepository workerProfileRepository;
    private final FileStorageService fileStorageService;

    public WorkerVerificationService(
            VerificationDocumentRepository verificationDocumentRepository,
            WorkerProfileRepository workerProfileRepository,
            FileStorageService fileStorageService
    ) {
        this.verificationDocumentRepository = verificationDocumentRepository;
        this.workerProfileRepository = workerProfileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public VerificationDocumentResponse upload(UUID callerId, DocumentType documentType, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidRequestException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new InvalidRequestException("File exceeds the 10MB limit");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidRequestException("Unsupported file type - use JPEG, PNG, or PDF");
        }

        WorkerProfile workerProfile = workerProfileRepository.findByUserId(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("No worker profile for this account"));

        // Keyed by worker profile id, not user id - if we ever let a worker
        // re-link a different login to the same profile (unlikely, but cheap
        // to get right now) the storage layout doesn't need to change.
        String storageKey = fileStorageService.store(file, workerProfile.getId().toString());

        VerificationDocument document = VerificationDocument.builder()
                .workerProfile(workerProfile)
                .documentType(documentType)
                .fileStorageKey(storageKey)
                .status(DocumentStatus.PENDING)
                .build();

        document = verificationDocumentRepository.save(document);
        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<VerificationDocumentResponse> listMine(UUID callerId) {
        WorkerProfile workerProfile = workerProfileRepository.findByUserId(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("No worker profile for this account"));

        return verificationDocumentRepository.findByWorkerProfileIdOrderBySubmittedAtDesc(workerProfile.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private VerificationDocumentResponse toResponse(VerificationDocument document) {
        return new VerificationDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getStatus(),
                document.getSubmittedAt(),
                document.getReviewedAt()
        );
    }
}
