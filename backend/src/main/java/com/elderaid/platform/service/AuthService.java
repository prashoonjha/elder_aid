package com.elderaid.platform.service;

import com.elderaid.platform.config.JwtProperties;
import com.elderaid.platform.domain.auth.RefreshToken;
import com.elderaid.platform.domain.consent.ConsentRecord;
import com.elderaid.platform.domain.consent.ConsentType;
import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.user.UserStatus;
import com.elderaid.platform.exception.EmailAlreadyInUseException;
import com.elderaid.platform.exception.InvalidCredentialsException;
import com.elderaid.platform.exception.InvalidRefreshTokenException;
import com.elderaid.platform.exception.InvalidRegistrationRoleException;
import com.elderaid.platform.repository.ConsentRecordRepository;
import com.elderaid.platform.repository.RefreshTokenRepository;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.security.JwtService;
import com.elderaid.platform.web.dto.AuthResponse;
import com.elderaid.platform.web.dto.LoginRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String CURRENT_TERMS_VERSION = "2026-06-01";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ConsentRecordRepository consentRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenExpirationDays;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            ConsentRecordRepository consentRecordRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            JwtProperties jwtProperties
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.consentRecordRepository = consentRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenExpirationDays = jwtProperties.getRefreshTokenExpirationDays();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String clientIp) {
        if (request.role() == UserRole.ADMIN) {
            throw new InvalidRegistrationRoleException();
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException(request.email());
        }

        Set<UserRole> roles = new HashSet<>();
        roles.add(request.role());

        AppUser user = AppUser.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .locale(request.locale() != null ? request.locale() : "fi")
                .status(UserStatus.ACTIVE)
                .roles(roles)
                .build();

        user = userRepository.save(user);

        // GDPR: record the consent that was given at registration, tied to the
        // policy version in effect right now - not just a boolean flag on the
        // user row. request.termsAccepted() is enforced by @AssertTrue on the
        // DTO, so reaching this point means consent was actually given.
        ConsentRecord consent = ConsentRecord.builder()
                .user(user)
                .consentType(ConsentType.TERMS_OF_SERVICE)
                .given(true)
                .policyVersion(CURRENT_TERMS_VERSION)
                .ipAddress(clientIp)
                .build();
        consentRecordRepository.save(consent);

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() != UserStatus.ACTIVE
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokenPair(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        String hash = hash(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (!existing.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        // Rotate: revoke the used token, issue a brand new pair. If a stolen
        // token is ever replayed after the legitimate user already rotated
        // it, this lookup simply won't find a valid row anymore.
        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        AppUser user = existing.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidRefreshTokenException();
        }

        return issueTokenPair(user);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse issueTokenPair(AppUser user) {
        String accessToken = jwtService.generateAccessToken(user);

        String rawRefreshToken = generateOpaqueToken();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hash(rawRefreshToken))
                .expiresAt(OffsetDateTime.now().plusDays(refreshTokenExpirationDays))
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(accessToken, rawRefreshToken, jwtService.getAccessTokenExpirationSeconds());
    }

    private String generateOpaqueToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes());
            return Base64.getEncoder().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
