package com.elderaid.platform.repository;

import com.elderaid.platform.domain.consent.ConsentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {

    List<ConsentRecord> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
