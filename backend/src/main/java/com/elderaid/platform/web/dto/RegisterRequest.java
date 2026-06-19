package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.user.UserRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email
        @NotNull
        String email,

        @Size(min = 10, message = "Password must be at least 10 characters")
        @NotNull
        String password,

        String phone,

        @NotNull
        UserRole role,

        @AssertTrue(message = "Terms of service and privacy policy must be accepted")
        boolean termsAccepted,

        String locale
) {
}
