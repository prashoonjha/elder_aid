package com.elderaid.platform.service;

import com.elderaid.platform.domain.elderly.ElderlyProfile;
import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.task.TaskRequest;
import com.elderaid.platform.domain.task.TaskStatus;
import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.exception.ForbiddenOperationException;
import com.elderaid.platform.exception.ResourceNotFoundException;
import com.elderaid.platform.repository.ElderlyProfileRepository;
import com.elderaid.platform.repository.TaskRequestRepository;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.web.dto.CreateTaskRequest;
import com.elderaid.platform.web.dto.TaskDetailResponse;
import com.elderaid.platform.web.dto.TaskSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRequestRepository taskRequestRepository;
    private final ElderlyProfileRepository elderlyProfileRepository;
    private final UserRepository userRepository;
    private final ElderlyProfileService elderlyProfileService;

    public TaskService(
            TaskRequestRepository taskRequestRepository,
            ElderlyProfileRepository elderlyProfileRepository,
            UserRepository userRepository,
            ElderlyProfileService elderlyProfileService
    ) {
        this.taskRequestRepository = taskRequestRepository;
        this.elderlyProfileRepository = elderlyProfileRepository;
        this.userRepository = userRepository;
        this.elderlyProfileService = elderlyProfileService;
    }

    @Transactional
    public TaskDetailResponse createTask(UUID callerId, CreateTaskRequest request) {
        // Throws if the caller has no link to this elderly profile, or only
        // has view-only access - no point loading anything else first.
        elderlyProfileService.requireBookingPermission(callerId, request.elderlyProfileId());

        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new ForbiddenOperationException("scheduledEnd must be after scheduledStart");
        }

        ElderlyProfile elderlyProfile = elderlyProfileRepository.findById(request.elderlyProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Elderly profile not found"));
        AppUser caller = userRepository.getReferenceById(callerId);

        TaskRequest task = TaskRequest.builder()
                .elderlyProfile(elderlyProfile)
                .postedByUser(caller)
                .category(request.category())
                .description(request.description())
                .locationLat(request.locationLat())
                .locationLng(request.locationLng())
                .addressLine(request.addressLine())
                .city(request.city())
                .scheduledStart(request.scheduledStart())
                .scheduledEnd(request.scheduledEnd())
                .priceOffered(request.priceOffered())
                .build();

        task = taskRequestRepository.save(task);
        return toDetail(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskSummaryResponse> browseOpenTasks(TaskCategory category, Pageable pageable) {
        return taskRequestRepository.browseOpen(category, OffsetDateTime.now(), pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<TaskDetailResponse> listMine(UUID callerId) {
        return taskRequestRepository.findByPostedByUserIdOrderByCreatedAtDesc(callerId).stream()
                .map(this::toDetail)
                .toList();
    }

    /**
     * Workers and the poster both end up here, so the response only ever
     * carries summary-level fields - exact address is withheld until a
     * booking is actually confirmed, not just at the application stage.
     */
    @Transactional(readOnly = true)
    public TaskSummaryResponse getTaskSummary(UUID taskId) {
        TaskRequest task = findTaskOrThrow(taskId);
        return toSummary(task);
    }

    @Transactional(readOnly = true)
    public TaskDetailResponse getOwnTaskDetail(UUID callerId, UUID taskId) {
        TaskRequest task = findTaskOrThrow(taskId);
        requireOwnership(callerId, task);
        return toDetail(task);
    }

    @Transactional
    public void cancel(UUID callerId, UUID taskId) {
        TaskRequest task = findTaskOrThrow(taskId);
        requireOwnership(callerId, task);

        if (task.getStatus() != TaskStatus.OPEN) {
            throw new ForbiddenOperationException("Only an open task can be cancelled directly");
        }
        task.setStatus(TaskStatus.CANCELLED);
        taskRequestRepository.save(task);
    }

    private TaskRequest findTaskOrThrow(UUID taskId) {
        return taskRequestRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }

    private void requireOwnership(UUID callerId, TaskRequest task) {
        if (!task.getPostedByUser().getId().equals(callerId)) {
            throw new ForbiddenOperationException("You do not own this task");
        }
    }

    private TaskSummaryResponse toSummary(TaskRequest task) {
        return new TaskSummaryResponse(
                task.getId(),
                task.getCategory(),
                task.getDescription(),
                task.getCity(),
                task.getScheduledStart(),
                task.getScheduledEnd(),
                task.getPriceOffered(),
                task.getStatus()
        );
    }

    private TaskDetailResponse toDetail(TaskRequest task) {
        return new TaskDetailResponse(
                task.getId(),
                task.getElderlyProfile().getId(),
                task.getCategory(),
                task.getDescription(),
                task.getLocationLat(),
                task.getLocationLng(),
                task.getAddressLine(),
                task.getCity(),
                task.getScheduledStart(),
                task.getScheduledEnd(),
                task.getPriceOffered(),
                task.getStatus(),
                task.getCreatedAt()
        );
    }
}
