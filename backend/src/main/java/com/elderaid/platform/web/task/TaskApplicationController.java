package com.elderaid.platform.web.task;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.TaskApplicationService;
import com.elderaid.platform.web.dto.ApplyToTaskRequest;
import com.elderaid.platform.web.dto.TaskApplicationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class TaskApplicationController {

    private final TaskApplicationService taskApplicationService;

    public TaskApplicationController(TaskApplicationService taskApplicationService) {
        this.taskApplicationService = taskApplicationService;
    }

    @PostMapping("/api/tasks/{taskId}/applications")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<TaskApplicationResponse> apply(
            @AuthenticationPrincipal CurrentUser caller,
            @PathVariable UUID taskId,
            @Valid @RequestBody ApplyToTaskRequest request
    ) {
        TaskApplicationResponse response = taskApplicationService.apply(caller.id(), taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Reviewing applications for a task you posted - ownership is checked
    // in the service, not here, since "is this my task" needs a DB lookup.
    @GetMapping("/api/tasks/{taskId}/applications")
    @PreAuthorize("hasAnyRole('CLIENT', 'FAMILY_MEMBER')")
    public List<TaskApplicationResponse> listForTask(
            @AuthenticationPrincipal CurrentUser caller,
            @PathVariable UUID taskId
    ) {
        return taskApplicationService.listForTask(caller.id(), taskId);
    }

    @GetMapping("/api/applications/mine")
    @PreAuthorize("hasRole('WORKER')")
    public List<TaskApplicationResponse> mine(@AuthenticationPrincipal CurrentUser caller) {
        return taskApplicationService.listMine(caller.id());
    }
}
