package com.elderaid.platform.repository;

import com.elderaid.platform.domain.worker.VerificationDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VerificationDocumentRepository extends JpaRepository<VerificationDocument, UUID> {

    List<VerificationDocument> findByWorkerProfileIdOrderBySubmittedAtDesc(UUID workerProfileId);
}
