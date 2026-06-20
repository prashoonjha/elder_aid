package com.elderaid.platform.repository;

import com.elderaid.platform.domain.worker.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {

    Optional<WorkerProfile> findByUserId(UUID userId);
}
