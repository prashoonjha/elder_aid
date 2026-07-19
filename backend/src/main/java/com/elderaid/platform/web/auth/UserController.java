package com.elderaid.platform.web.auth;

import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.worker.VerificationStatus;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.security.CurrentUser;
import com.elderaid.platform.service.DataPrivacyService;
import com.elderaid.platform.web.dto.CurrentUserResponse;
import com.elderaid.platform.web.dto.DeleteAccountRequest;
import com.elderaid.platform.web.dto.UserDataExportResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DataPrivacyService dataPrivacyService;
    private final UserRepository userRepository;
    private final WorkerProfileRepository workerProfileRepository;

    public UserController(
            DataPrivacyService dataPrivacyService,
            UserRepository userRepository,
            WorkerProfileRepository workerProfileRepository
    ) {
        this.dataPrivacyService = dataPrivacyService;
        this.userRepository = userRepository;
        this.workerProfileRepository = workerProfileRepository;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        AppUser user = userRepository.findById(currentUser.id()).orElseThrow();

        // Only workers have a profile, so this stays null for everyone else.
        VerificationStatus status = workerProfileRepository.findByUserId(currentUser.id())
                .map(profile -> profile.getVerificationStatus())
                .orElse(null);

        return new CurrentUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                currentUser.roles(),
                status
        );
    }

    // GDPR Article 15/20 - everything we hold tied to this account, in one
    // response the person can save or hand to another service.
    @GetMapping("/me/data-export")
    public UserDataExportResponse exportMyData(@AuthenticationPrincipal CurrentUser currentUser) {
        return dataPrivacyService.exportMyData(currentUser.id());
    }

    // GDPR Article 17 - this anonymizes rather than deletes rows outright;
    // see DataPrivacyService for why.
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(
            @AuthenticationPrincipal CurrentUser currentUser,
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        dataPrivacyService.deleteMyAccount(currentUser.id(), request.password());
        return ResponseEntity.noContent().build();
    }
}
