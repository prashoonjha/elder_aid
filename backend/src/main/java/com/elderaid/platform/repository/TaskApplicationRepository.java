package com.elderaid.platform.repository;

import com.elderaid.platform.domain.task.TaskApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskApplicationRepository extends JpaRepository<TaskApplication, UUID> {

    List<TaskApplication> findByTaskRequestId(UUID taskRequestId);

    List<TaskApplication> findByWorkerProfileIdOrderByAppliedAtDesc(UUID workerProfileId);

    Optional<TaskApplication> findByTaskRequestIdAndWorkerProfileId(UUID taskRequestId, UUID workerProfileId);
}
