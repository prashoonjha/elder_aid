package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.worker.VerificationTier;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        String firstName,
        Set<UserRole> roles,
        // null for non-workers
        VerificationTier verificationTier
) {
}
