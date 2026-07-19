package com.elderaid.platform.task;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.worker.VerificationStatus;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.ApplyToTaskRequest;
import com.elderaid.platform.web.dto.CreateElderlyProfileRequest;
import com.elderaid.platform.web.dto.CreateTaskRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TaskFlowIntegrationTest {

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

    @Autowired
    private WorkerProfileRepository workerProfileRepository;

    @Test
    void familyMemberPostsTask_workerApplies_afterVerification() throws Exception {
        String familyToken = registerAndGetAccessToken(
                "aiti.tytar@example.com", "TurvallinenSalasana1", UserRole.FAMILY_MEMBER);

        RegisterRequest workerRegistration = new RegisterRequest(
                "opiskelija@example.com", "Opiskelija", "Helppari",
                "TurvallinenSalasana1", null, UserRole.WORKER, true, "fi");
        String workerRegisterJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(workerRegistration)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String workerAccessToken = objectMapper.readTree(workerRegisterJson).get("accessToken").asText();
        UUID workerUserId = extractUserId(workerAccessToken);

        // No admin review flow exists yet (that's the next feature) - bump
        // the tier directly so this test can exercise the apply path now.
        var workerProfile = workerProfileRepository.findByUserId(workerUserId).orElseThrow();
        workerProfile.setVerificationStatus(VerificationStatus.VERIFIED);
        workerProfileRepository.save(workerProfile);

        CreateElderlyProfileRequest elderlyRequest = new CreateElderlyProfileRequest(
                "Aino", "Virtanen", null, "Mannerheimintie 1", "Helsinki", "00100", "fi", "daughter", false);

        String elderlyResponseJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(elderlyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID elderlyProfileId = UUID.fromString(objectMapper.readTree(elderlyResponseJson).get("id").asText());

        CreateTaskRequest taskRequest = new CreateTaskRequest(
                elderlyProfileId,
                TaskCategory.GROCERY_SHOPPING,
                "Weekly grocery run, list will be provided",
                null, null,
                "Mannerheimintie 1", "Helsinki",
                OffsetDateTime.now().plusDays(1),
                OffsetDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("25.00")
        );

        String taskResponseJson = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addressLine").value("Mannerheimintie 1"))
                .andReturn().getResponse().getContentAsString();
        UUID taskId = UUID.fromString(objectMapper.readTree(taskResponseJson).get("id").asText());

        // The public browse listing must NOT leak the exact address.
        mockMvc.perform(get("/api/tasks/" + taskId)
                        .header("Authorization", "Bearer " + workerAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Helsinki"));

        ApplyToTaskRequest applyRequest = new ApplyToTaskRequest("Happy to help with this!");

        mockMvc.perform(post("/api/tasks/" + taskId + "/applications")
                        .header("Authorization", "Bearer " + workerAccessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/tasks/" + taskId + "/applications")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].workerFirstName").value("Opiskelija"));
    }

    @Test
    void elderlyClientCreatesOwnProfileAndPostsOwnTask() throws Exception {
        String clientToken = registerAndGetAccessToken(
                "ikaihminen@example.com", "TurvallinenSalasana1", UserRole.CLIENT);

        CreateElderlyProfileRequest selfProfileRequest = new CreateElderlyProfileRequest(
                "Pirkko", "Korhonen", null, "Aleksanterinkatu 5", "Turku", "20100", "fi", null, true);

        String profileResponseJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(selfProfileRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.relationship").value("self"))
                .andReturn().getResponse().getContentAsString();
        UUID profileId = UUID.fromString(objectMapper.readTree(profileResponseJson).get("id").asText());

        CreateTaskRequest taskRequest = new CreateTaskRequest(
                profileId,
                TaskCategory.WALKING_COMPANION,
                "A short walk around the block",
                null, null,
                "Aleksanterinkatu 5", "Turku",
                OffsetDateTime.now().plusDays(2),
                OffsetDateTime.now().plusDays(2).plusHours(1),
                new BigDecimal("15.00")
        );

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated());
    }

    private String registerAndGetAccessToken(String email, String password, UserRole role) throws Exception {
        RegisterRequest request = new RegisterRequest(
                email, "Etu", "Suku", password, null, role, true, "fi");
        String responseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("accessToken").asText();
    }

    private UUID extractUserId(String accessToken) {
        // Access tokens are JWTs - the payload is the middle base64url segment.
        String[] parts = accessToken.split("\\.");
        byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
        try {
            JsonNode payload = objectMapper.readTree(payloadBytes);
            return UUID.fromString(payload.get("sub").asText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
