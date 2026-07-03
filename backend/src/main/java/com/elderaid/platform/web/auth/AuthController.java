package com.elderaid.platform.web.auth;

import com.elderaid.platform.service.AuthService;
import com.elderaid.platform.web.dto.AuthResponse;
import com.elderaid.platform.web.dto.LoginRequest;
import com.elderaid.platform.web.dto.RefreshRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final RefreshCookieFactory refreshCookieFactory;

    public AuthController(AuthService authService, RefreshCookieFactory refreshCookieFactory) {
        this.authService = authService;
        this.refreshCookieFactory = refreshCookieFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.register(request, httpRequest.getRemoteAddr());
        httpResponse.addCookie(refreshCookieFactory.build(response.refreshToken()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request);
        httpResponse.addCookie(refreshCookieFactory.build(response.refreshToken()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        // Prefer the httpOnly cookie; fall back to the request body so the
        // existing body-based frontend keeps working until it's migrated.
        String refreshToken = readRefreshFromCookie(httpRequest)
                .orElseGet(() -> request != null ? request.refreshToken() : null);

        AuthResponse response = authService.refresh(refreshToken);
        httpResponse.addCookie(refreshCookieFactory.build(response.refreshToken()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String refreshToken = readRefreshFromCookie(httpRequest)
                .orElseGet(() -> request != null ? request.refreshToken() : null);

        if (refreshToken != null) {
            authService.logout(refreshToken);
        }
        httpResponse.addCookie(refreshCookieFactory.clearing());
        return ResponseEntity.noContent().build();
    }

    private Optional<String> readRefreshFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> refreshCookieFactory.name().equals(c.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }
}
