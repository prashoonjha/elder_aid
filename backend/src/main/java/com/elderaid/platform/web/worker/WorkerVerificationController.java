package com.elderaid.platform.web.worker;

import com.elderaid.platform.domain.worker.DocumentType;
import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.WorkerVerificationService;
import com.elderaid.platform.web.dto.VerificationDocumentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/workers/me/verification-documents")
@PreAuthorize("hasRole('WORKER')")
public class WorkerVerificationController {

    private final WorkerVerificationService workerVerificationService;

    public WorkerVerificationController(WorkerVerificationService workerVerificationService) {
        this.workerVerificationService = workerVerificationService;
    }

    @PostMapping
    public ResponseEntity<VerificationDocumentResponse> upload(
            @AuthenticationPrincipal CurrentUser caller,
            @RequestParam DocumentType documentType,
            @RequestParam MultipartFile file
    ) {
        VerificationDocumentResponse response = workerVerificationService.upload(caller.id(), documentType, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<VerificationDocumentResponse> mine(@AuthenticationPrincipal CurrentUser caller) {
        return workerVerificationService.listMine(caller.id());
    }
}
