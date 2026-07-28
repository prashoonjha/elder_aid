package com.elderaid.platform.web.dto;

import com.elderaid.platform.domain.user.UserRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @Email
        @NotNull
        String email,

        @NotNull
        @Size(min = 1, max = 100)
        String firstName,

        @NotNull
        @Size(min = 1, max = 100)
        String lastName,

        @Size(min = 10, message = "Password must be at least 10 characters")
        @NotNull
        String password,

        // Optional, but if given must be a Finnish mobile: +358 then 4 or 5
        // and 8 more digits. The frontend sends it already normalised.
        @Pattern(regexp = "^\\+358[45]\\d{8}$", message = "Phone must be a valid Finnish mobile number")
        String phone,

        @NotNull
        UserRole role,

        @AssertTrue(message = "Terms of service and privacy policy must be accepted")
        boolean termsAccepted,

        String locale
) {
}
