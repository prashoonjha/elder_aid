package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.user.UserRole;

import java.util.Set;
import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email,
        Set<UserRole> roles
) {
}
