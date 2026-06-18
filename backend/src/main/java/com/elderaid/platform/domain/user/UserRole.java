package com.elderaid.platform.domain.user;

/**
 * A single account can hold more than one role at a time (e.g. a family
 * manager who also occasionally books help for themselves), which is why
 * this is a Set on AppUser rather than a single column.
 */
public enum UserRole {
    CLIENT,
    FAMILY_MEMBER,
    WORKER,
    ADMIN
}
