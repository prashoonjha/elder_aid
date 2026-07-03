package com.elderaid.platform.auth;

import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.web.dto.LoginRequest;
import com.elderaid.platform.web.dto.RefreshRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerThenLoginThenRefreshThenLogout() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "testi.kayttaja@example.com",
                "Testi",
                "Kayttaja",
                "SalasanaOnPitka123",
                "+358401234567",
                UserRole.CLIENT,
                true,
                "fi"
        );

        String registerResponseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn().getResponse().getContentAsString();

        String firstRefreshToken = objectMapper.readTree(registerResponseJson).get("refreshToken").asText();

        LoginRequest loginRequest = new LoginRequest("testi.kayttaja@example.com", "SalasanaOnPitka123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        RefreshRequest refreshRequest = new RefreshRequest(firstRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // The first refresh token was rotated out by the call above, so
        // reusing it now must fail - this is the replay-protection guarantee.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithWrongPasswordIsRejected() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "toinen.kayttaja@example.com",
                "Toinen",
                "Kayttaja",
                "SalasanaOnPitka123",
                null,
                UserRole.WORKER,
                true,
                "fi"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest badLogin = new LoginRequest("toinen.kayttaja@example.com", "wrong-password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSetsHttpOnlyRefreshCookieAndRefreshWorksFromCookieAlone() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "keksi.kayttaja@example.com", "Keksi", "Kayttaja", "TurvallinenSalasana1",
                null, UserRole.CLIENT, true, "fi");
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("keksi.kayttaja@example.com", "TurvallinenSalasana1");
        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("elderaid_refresh"))
                .andExpect(cookie().httpOnly("elderaid_refresh", true))
                .andReturn();

        Cookie refreshCookie = loginResult.getResponse().getCookie("elderaid_refresh");

        // Refresh using ONLY the cookie - no request body at all. This is
        // what the migrated frontend will do.
        mockMvc.perform(post("/api/auth/refresh").cookie(refreshCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(cookie().exists("elderaid_refresh"));

        // Logout clears the cookie (max-age 0).
        mockMvc.perform(post("/api/auth/logout").cookie(refreshCookie))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("elderaid_refresh", 0));
    }
}
