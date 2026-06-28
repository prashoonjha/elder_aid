package com.elderaid.platform.booking;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.worker.VerificationTier;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class BookingFlowIntegrationTest {

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
    void acceptingOneApplicationRejectsTheOthersAndMatchesTheTask() throws Exception {
        String familyToken = registerAndGetAccessToken("perhe@example.com", UserRole.FAMILY_MEMBER);

        CreateElderlyProfileRequest elderlyRequest = new CreateElderlyProfileRequest(
                "Eero", "Maki", null, "Aleksanterinkatu 1", "Turku", "20100", "fi", "son", false);
        String profileJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(elderlyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID profileId = UUID.fromString(objectMapper.readTree(profileJson).get("id").asText());

        CreateTaskRequest taskRequest = new CreateTaskRequest(
                profileId, TaskCategory.HOUSEHOLD_HELP, "Siivousapua",
                null, null, "Aleksanterinkatu 1", "Turku",
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(2),
                new BigDecimal("30.00"));
        String taskJson = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID taskId = UUID.fromString(objectMapper.readTree(taskJson).get("id").asText());

        String worker1Token = registerVerifiedWorkerAndGetAccessToken("tyontekija1@example.com");
        String worker2Token = registerVerifiedWorkerAndGetAccessToken("tyontekija2@example.com");

        UUID application1Id = applyAndGetApplicationId(taskId, worker1Token);
        UUID application2Id = applyAndGetApplicationId(taskId, worker2Token);

        // Accept worker 1's application.
        String bookingJson = mockMvc.perform(patch("/api/tasks/" + taskId + "/applications/" + application1Id + "/accept")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.taskCategory").value("HOUSEHOLD_HELP"))
                .andExpect(jsonPath("$.taskCity").value("Turku"))
                .andReturn().getResponse().getContentAsString();
        UUID workerProfileIdOnBooking = UUID.fromString(objectMapper.readTree(bookingJson).get("workerProfileId").asText());
        org.junit.jupiter.api.Assertions.assertNotNull(workerProfileIdOnBooking);

        // The task itself should now show as MATCHED to its owner.
        mockMvc.perform(get("/api/tasks/mine/" + taskId)
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MATCHED"));

        // Worker 2's application should have been auto-rejected, not left pending.
        String applicationsJson = mockMvc.perform(get("/api/tasks/" + taskId + "/applications")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode applications = objectMapper.readTree(applicationsJson);
        for (JsonNode app : applications) {
            String id = app.get("id").asText();
            String expectedStatus = id.equals(application2Id.toString()) ? "REJECTED" : "ACCEPTED";
            org.junit.jupiter.api.Assertions.assertEquals(expectedStatus, app.get("status").asText());
        }

        // Trying to accept the already-rejected application should fail cleanly.
        mockMvc.perform(patch("/api/tasks/" + taskId + "/applications/" + application2Id + "/accept")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void workerCanCheckInAndOutOnlyForTheirOwnBooking() throws Exception {
        String familyToken = registerAndGetAccessToken("perhe2@example.com", UserRole.FAMILY_MEMBER);

        CreateElderlyProfileRequest elderlyRequest = new CreateElderlyProfileRequest(
                "Liisa", "Nieminen", null, "Hameenkatu 5", "Tampere", "33100", "fi", "daughter", false);
        String profileJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(elderlyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID profileId = UUID.fromString(objectMapper.readTree(profileJson).get("id").asText());

        CreateTaskRequest taskRequest = new CreateTaskRequest(
                profileId, TaskCategory.WALKING_COMPANION, "Ulkoilua",
                null, null, "Hameenkatu 5", "Tampere",
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("18.00"));
        String taskJson = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID taskId = UUID.fromString(objectMapper.readTree(taskJson).get("id").asText());

        String assignedWorkerToken = registerVerifiedWorkerAndGetAccessToken("hyvaksytty.tyontekija@example.com");
        String otherWorkerToken = registerVerifiedWorkerAndGetAccessToken("toinen.tyontekija@example.com");

        UUID applicationId = applyAndGetApplicationId(taskId, assignedWorkerToken);

        String bookingJson = mockMvc.perform(patch("/api/tasks/" + taskId + "/applications/" + applicationId + "/accept")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID bookingId = UUID.fromString(objectMapper.readTree(bookingJson).get("id").asText());

        // A worker who isn't assigned to this booking must not be able to check in.
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-in")
                        .header("Authorization", "Bearer " + otherWorkerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-in")
                        .header("Authorization", "Bearer " + assignedWorkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"))
                .andExpect(jsonPath("$.checkInTime").exists());

        // Checking in twice should be rejected - the state machine only
        // moves forward, never re-enters a state it already left.
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-in")
                        .header("Authorization", "Bearer " + assignedWorkerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-out")
                        .header("Authorization", "Bearer " + assignedWorkerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.checkOutTime").exists());

        // The underlying task should now read COMPLETED too, not just the booking.
        mockMvc.perform(get("/api/tasks/mine/" + taskId)
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // Both the worker and the family member can look the booking up by task.
        mockMvc.perform(get("/api/tasks/" + taskId + "/booking")
                        .header("Authorization", "Bearer " + assignedWorkerToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/tasks/" + taskId + "/booking")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk());

        // An uninvolved worker should not be able to look it up.
        mockMvc.perform(get("/api/tasks/" + taskId + "/booking")
                        .header("Authorization", "Bearer " + otherWorkerToken))
                .andExpect(status().isForbidden());
    }

    private String registerAndGetAccessToken(String email, UserRole role) throws Exception {
        RegisterRequest request = new RegisterRequest(email, "Etu", "Suku", "TurvallinenSalasana1", null, role, true, "fi");
        String responseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(responseJson).get("accessToken").asText();
    }

    private String registerVerifiedWorkerAndGetAccessToken(String email) throws Exception {
        String accessToken = registerAndGetAccessToken(email, UserRole.WORKER);
        UUID userId = extractUserId(accessToken);
        var workerProfile = workerProfileRepository.findByUserId(userId).orElseThrow();
        workerProfile.setVerificationTier(VerificationTier.TIER1_ID_VERIFIED);
        workerProfileRepository.save(workerProfile);
        return accessToken;
    }

    private UUID applyAndGetApplicationId(UUID taskId, String workerToken) throws Exception {
        ApplyToTaskRequest applyRequest = new ApplyToTaskRequest("Voin auttaa!");
        String responseJson = mockMvc.perform(post("/api/tasks/" + taskId + "/applications")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(responseJson).get("id").asText());
    }

    private UUID extractUserId(String accessToken) throws Exception {
        String[] parts = accessToken.split("\\.");
        byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
        return UUID.fromString(objectMapper.readTree(payloadBytes).get("sub").asText());
    }
}
