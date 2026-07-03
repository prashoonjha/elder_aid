package com.elderaid.platform.web.auth;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Builds the refresh-token cookie. httpOnly so JavaScript can't read it
 * (the whole point - an XSS payload has no way to steal the token), and
 * SameSite=Lax so it isn't sent on cross-site requests, which is the CSRF
 * mitigation given the cookie is otherwise sent automatically by the browser.
 */
@Component
public class RefreshCookieFactory {

    private final String cookieName;
    private final boolean secure;
    private final long maxAgeSeconds;

    public RefreshCookieFactory(
            @Value("${app.auth.refresh-cookie-name}") String cookieName,
            @Value("${app.auth.refresh-cookie-secure}") boolean secure,
            @Value("${app.jwt.refresh-token-expiration-days}") long refreshExpirationDays
    ) {
        this.cookieName = cookieName;
        this.secure = secure;
        this.maxAgeSeconds = Duration.ofDays(refreshExpirationDays).toSeconds();
    }

    public String name() {
        return cookieName;
    }

    public Cookie build(String refreshToken) {
        Cookie cookie = new Cookie(cookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) maxAgeSeconds);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }

    /**
     * A cookie with the same name and path but zero max-age tells the
     * browser to delete it - used on logout.
     */
    public Cookie clearing() {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}
