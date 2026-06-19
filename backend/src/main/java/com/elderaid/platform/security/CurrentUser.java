package com.elderaid.platform.security;

import com.elderaid.platform.domain.user.UserRole;

import java.util.Set;
import java.util.UUID;

/**
 * What ends up as Authentication#getPrincipal() after a request passes the
 * JWT filter. Deliberately not the full AppUser entity - controllers should
 * go back to the repository if they need more than id/email/roles, rather
 * than carrying a detached entity around the request.
 */
public record CurrentUser(UUID id, String email, Set<UserRole> roles) {
}
