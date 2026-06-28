package com.elderaid.platform.web.auth;

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

    public UserController(DataPrivacyService dataPrivacyService) {
        this.dataPrivacyService = dataPrivacyService;
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal CurrentUser currentUser) {
        return new CurrentUserResponse(currentUser.id(), currentUser.email(), currentUser.roles());
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
