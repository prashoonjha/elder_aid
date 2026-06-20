package com.elderaid.platform.web.task;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.TaskService;
import com.elderaid.platform.web.dto.CreateTaskRequest;
import com.elderaid.platform.web.dto.TaskDetailResponse;
import com.elderaid.platform.web.dto.TaskSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public ResponseEntity<TaskDetailResponse> create(
            @AuthenticationPrincipal CurrentUser caller,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskDetailResponse response = taskService.createTask(caller.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // No role restriction here on purpose - anyone logged in can see what's
    // on the open task board, the same way a job board works. Applying is
    // what's gated to workers, not browsing.
    @GetMapping
    public Page<TaskSummaryResponse> browse(
            @RequestParam(required = false) TaskCategory category,
            Pageable pageable
    ) {
        return taskService.browseOpenTasks(category, pageable);
    }

    @GetMapping("/{taskId}")
    public TaskSummaryResponse getOne(@PathVariable UUID taskId) {
        return taskService.getTaskSummary(taskId);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public List<TaskDetailResponse> mine(@AuthenticationPrincipal CurrentUser caller) {
        return taskService.listMine(caller.id());
    }

    @GetMapping("/mine/{taskId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public TaskDetailResponse mineDetail(
            @AuthenticationPrincipal CurrentUser caller,
            @PathVariable UUID taskId
    ) {
        return taskService.getOwnTaskDetail(caller.id(), taskId);
    }

    @PatchMapping("/{taskId}/cancel")
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal CurrentUser caller,
            @PathVariable UUID taskId
    ) {
        taskService.cancel(caller.id(), taskId);
        return ResponseEntity.noContent().build();
    }
}
