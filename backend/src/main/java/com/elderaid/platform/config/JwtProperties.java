package com.elderaid.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Base64-encoded HMAC secret. Must be overridden via the JWT_SECRET
     * environment variable outside local dev - never commit a real secret.
     */
    private String secret;

    private long accessTokenExpirationMinutes = 15;

    private long refreshTokenExpirationDays = 7;
}
