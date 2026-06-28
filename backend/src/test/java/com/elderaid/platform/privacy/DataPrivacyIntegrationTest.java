package com.elderaid.platform.privacy;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.web.dto.CreateElderlyProfileRequest;
import com.elderaid.platform.web.dto.CreateTaskRequest;
import com.elderaid.platform.web.dto.DeleteAccountRequest;
import com.elderaid.platform.web.dto.LoginRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class DataPrivacyIntegrationTest {

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
    void exportIncludesConsentProfilesAndPostedTasks() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "vienosti@example.com", "Vieno", "Tieto", "TurvallinenSalasana1", null,
                UserRole.FAMILY_MEMBER, true, "fi");
        String registerJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registerJson).get("accessToken").asText();

        CreateElderlyProfileRequest elderlyRequest = new CreateElderlyProfileRequest(
                "Aimo", "Virta", null, "Puistokatu 2", "Lahti", "15100", "fi", "mother", false);
        String profileJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(elderlyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID profileId = UUID.fromString(objectMapper.readTree(profileJson).get("id").asText());

        CreateTaskRequest taskRequest = new CreateTaskRequest(
                profileId, TaskCategory.OTHER, "Asioiden hoitoa",
                null, null, "Puistokatu 2", "Lahti",
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("20.00"));
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users/me/data-export")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account.email").value("vienosti@example.com"))
                .andExpect(jsonPath("$.consents.length()").value(1))
                .andExpect(jsonPath("$.consents[0].consentType").value("TERMS_OF_SERVICE"))
                .andExpect(jsonPath("$.elderlyProfilesManaged.length()").value(1))
                .andExpect(jsonPath("$.elderlyProfilesManaged[0].firstName").value("Aimo"))
                .andExpect(jsonPath("$.tasksPosted.length()").value(1))
                .andExpect(jsonPath("$.workerProfile").value(nullValue()));
    }

    @Test
    void deletingAccountAnonymizesItAndRevokesSessions() throws Exception {
        String email = "poistettava@example.com";
        String password = "TurvallinenSalasana1";

        RegisterRequest registerRequest = new RegisterRequest(
                email, "Pekka", "Poistaja", password, null, UserRole.CLIENT, true, "fi");
        String registerJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registerJson).get("accessToken").asText();
        String refreshToken = objectMapper.readTree(registerJson).get("refreshToken").asText();

        // Wrong password should be rejected and change nothing.
        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteAccountRequest("WrongPassword1"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new DeleteAccountRequest(password))))
                .andExpect(status().isNoContent());

        // The original email no longer authenticates anything.
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new LoginRequest(email, password))))
                .andExpect(status().isUnauthorized());

        // The refresh token issued before deletion must not still work.
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isUnauthorized());
    }
}
