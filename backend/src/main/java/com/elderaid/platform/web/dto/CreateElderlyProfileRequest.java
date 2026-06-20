package com.elderaid.platform.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateElderlyProfileRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        LocalDate dateOfBirth,

        String addressLine,
        String city,
        String postalCode,
        String preferredLanguage,

        // How the caller relates to this person - "daughter", "son",
        // "neighbour", whatever they type. Free text on purpose: a fixed
        // enum would inevitably miss someone's actual situation. Required
        // unless forSelf is true, in which case it's set to "self"
        // automatically and this field is ignored.
        String relationship,

        // True when an elderly person is creating their own profile rather
        // than a family member creating it on their behalf. Deliberately
        // explicit instead of inferring it from the caller's role, since a
        // CLIENT account managing a parent's profile is a real case too.
        Boolean forSelf
) {
}
