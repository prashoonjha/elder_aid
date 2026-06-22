package com.elderaid.platform.web.admin;

import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.AdminVerificationService;
import com.elderaid.platform.web.dto.AdminVerificationDocumentResponse;
import com.elderaid.platform.web.dto.RejectDocumentRequest;
import jakarta.validation.Valid;
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
@RequestMapping("/api/admin/verification-documents")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVerificationController {

    private final AdminVerificationService adminVerificationService;

    public AdminVerificationController(AdminVerificationService adminVerificationService) {
        this.adminVerificationService = adminVerificationService;
    }

    @GetMapping
    public List<AdminVerificationDocumentResponse> listPending() {
        return adminVerificationService.listPending();
    }

    @PostMapping("/{documentId}/approve")
    public AdminVerificationDocumentResponse approve(
            @AuthenticationPrincipal CurrentUser admin,
            @PathVariable UUID documentId
    ) {
        return adminVerificationService.approve(admin.id(), documentId);
    }

    @PostMapping("/{documentId}/reject")
    public AdminVerificationDocumentResponse reject(
            @AuthenticationPrincipal CurrentUser admin,
            @PathVariable UUID documentId,
            @Valid @RequestBody RejectDocumentRequest request
    ) {
        return adminVerificationService.reject(admin.id(), documentId, request.reason());
    }
}
