package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.elderly.PermissionLevel;

import java.time.LocalDate;
import java.util.UUID;

public record ElderlyProfileResponse(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String addressLine,
        String city,
        String postalCode,
        String preferredLanguage,
        String relationship,
        PermissionLevel permissionLevel
) {
}
