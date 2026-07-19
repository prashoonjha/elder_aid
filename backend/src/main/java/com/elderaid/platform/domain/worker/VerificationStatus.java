package com.elderaid.platform.domain.worker;

public enum VerificationStatus {
    // Nothing submitted, or nothing approved yet.
    UNVERIFIED,
    // At least one document submitted and awaiting review.
    PENDING,
    // ID card and selfie both approved.
    VERIFIED
}
