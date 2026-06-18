package com.elderaid.platform.domain.worker;

/**
 * NONE: just signed up, can't take any tasks yet.
 * TIER1_ID_VERIFIED: ID document + selfie liveness check passed, can take
 *   low-risk/remote tasks (e.g. video companionship).
 * TIER2_BACKGROUND_CHECKED: criminal record extract reviewed and approved
 *   by an admin, required for any in-home or in-person task.
 */
public enum VerificationTier {
    NONE,
    TIER1_ID_VERIFIED,
    TIER2_BACKGROUND_CHECKED
}
