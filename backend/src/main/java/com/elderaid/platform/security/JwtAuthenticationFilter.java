package com.elderaid.platform.security;

import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.user.UserStatus;
import com.elderaid.platform.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Deliberately re-reads the user from the database on every request rather
 * than trusting the roles baked into the token. This costs one extra query
 * per request, but it means suspending a user or changing their roles takes
 * effect immediately, instead of waiting up to 15 minutes for their access
 * token to expire. Worth it at this scale.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());

        try {
            var claims = jwtService.parseAndValidate(token);
            UUID userId = jwtService.extractUserId(claims);

            Optional<AppUser> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent() && userOpt.get().getStatus() == UserStatus.ACTIVE) {
                AppUser user = userOpt.get();

                List<GrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .map(GrantedAuthority.class::cast)
                        .toList();

                CurrentUser principal = new CurrentUser(user.getId(), user.getEmail(), user.getRoles());

                var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // If the user is missing or no longer active, we simply leave the
            // SecurityContext empty - downstream authorization will reject the
            // request with 401/403 rather than us throwing here.
        } catch (JwtException | IllegalArgumentException ex) {
            // Invalid/expired token - leave unauthenticated, let Spring Security
            // return 401 for endpoints that require it.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
