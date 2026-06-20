package com.elderaid.platform.service;

import com.elderaid.platform.domain.task.ApplicationStatus;
import com.elderaid.platform.domain.task.TaskApplication;
import com.elderaid.platform.domain.task.TaskRequest;
import com.elderaid.platform.domain.task.TaskStatus;
import com.elderaid.platform.domain.worker.VerificationTier;
import com.elderaid.platform.domain.worker.WorkerProfile;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.TaskApplicationRepository;
import com.elderaid.platform.repository.TaskRequestRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.ApplyToTaskRequest;
import com.elderaid.platform.web.dto.TaskApplicationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskApplicationService {

    private final TaskApplicationRepository taskApplicationRepository;
    private final TaskRequestRepository taskRequestRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public TaskApplicationService(
            TaskApplicationRepository taskApplicationRepository,
            TaskRequestRepository taskRequestRepository,
            WorkerProfileRepository workerProfileRepository
    ) {
        this.taskApplicationRepository = taskApplicationRepository;
        this.taskRequestRepository = taskRequestRepository;
        this.workerProfileRepository = workerProfileRepository;
    }

    @Transactional
    public TaskApplicationResponse apply(UUID callerId, UUID taskId, ApplyToTaskRequest request) {
        WorkerProfile workerProfile = workerProfileRepository.findByUserId(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("No worker profile for this account"));

        // NONE-tier workers haven't passed even basic ID verification yet -
        // block applications until the verification step is in place. Bump a
        // worker's tier manually in the DB for now to test past this.
        if (workerProfile.getVerificationTier() == VerificationTier.NONE) {
            throw new ForbiddenOperationException("Account must complete identity verification before applying to tasks");
        }

        TaskRequest task = taskRequestRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new ForbiddenOperationException("This task is no longer accepting applications");
        }

        taskApplicationRepository.findByTaskRequestIdAndWorkerProfileId(taskId, workerProfile.getId())
                .ifPresent(existing -> {
                    throw new ForbiddenOperationException("You have already applied to this task");
                });

        TaskApplication application = TaskApplication.builder()
                .taskRequest(task)
                .workerProfile(workerProfile)
                .message(request.message())
                .status(ApplicationStatus.PENDING)
                .build();

        application = taskApplicationRepository.save(application);
        return toResponse(application);
    }

    /**
     * Only the person who posted the task gets to see who applied - an
     * applicant's identity isn't public the way the task listing itself is.
     */
    @Transactional(readOnly = true)
    public List<TaskApplicationResponse> listForTask(UUID callerId, UUID taskId) {
        TaskRequest task = taskRequestRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getPostedByUser().getId().equals(callerId)) {
            throw new ForbiddenOperationException("You do not own this task");
        }

        return taskApplicationRepository.findByTaskRequestId(taskId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskApplicationResponse> listMine(UUID callerId) {
        WorkerProfile workerProfile = workerProfileRepository.findByUserId(callerId)
                .orElseThrow(() -> new ResourceNotFoundException("No worker profile for this account"));

        return taskApplicationRepository.findByWorkerProfileIdOrderByAppliedAtDesc(workerProfile.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private TaskApplicationResponse toResponse(TaskApplication application) {
        WorkerProfile worker = application.getWorkerProfile();
        return new TaskApplicationResponse(
                application.getId(),
                application.getTaskRequest().getId(),
                worker.getId(),
                worker.getUser().getFirstName(),
                worker.getUser().getLastName(),
                worker.getAverageRating(),
                application.getStatus(),
                application.getMessage(),
                application.getAppliedAt()
        );
    }
}
