package com.elderaid.platform.review;

import com.elderaid.platform.domain.task.TaskCategory;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.worker.VerificationTier;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.web.dto.ApplyToTaskRequest;
import com.elderaid.platform.web.dto.CreateElderlyProfileRequest;
import com.elderaid.platform.web.dto.CreateReviewRequest;
import com.elderaid.platform.web.dto.CreateTaskRequest;
import com.elderaid.platform.web.dto.RegisterRequest;
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
class ReviewFlowIntegrationTest {

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
    void posterCanReviewCompletedBookingAndItUpdatesWorkerRating() throws Exception {
        Fixture f = createCompletedBooking("arvio.perhe@example.com", "arvio.tyontekija@example.com");

        // Can't review before the booking is completed? Here it IS completed,
        // so the happy path: poster submits a 4-star review.
        CreateReviewRequest review = new CreateReviewRequest(4, "Ystavallinen ja tasmallinen.");
        mockMvc.perform(post("/api/bookings/" + f.bookingId + "/review")
                        .header("Authorization", "Bearer " + f.familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.bookingId").value(f.bookingId.toString()));

        // The worker's stored average should now reflect the single review.
        var workerProfile = workerProfileRepository.findByUserId(f.workerUserId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(0, workerProfile.getAverageRating().compareTo(new BigDecimal("4.00")));
        org.junit.jupiter.api.Assertions.assertEquals(1, workerProfile.getReviewCount());

        // Reviewing the same booking twice is rejected.
        mockMvc.perform(post("/api/bookings/" + f.bookingId + "/review")
                        .header("Authorization", "Bearer " + f.familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ALREADY_REVIEWED"));
    }

    @Test
    void onlyTheTaskPosterCanReviewTheBooking() throws Exception {
        Fixture f = createCompletedBooking("arvio.perhe2@example.com", "arvio.tyontekija2@example.com");

        // A different family member who didn't post this task can't review it.
        String outsiderToken = registerAndGetAccessToken("ulkopuolinen@example.com", UserRole.FAMILY_MEMBER);

        CreateReviewRequest review = new CreateReviewRequest(5, null);
        mockMvc.perform(post("/api/bookings/" + f.bookingId + "/review")
                        .header("Authorization", "Bearer " + outsiderToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("REVIEW_NOT_POSTER"));
    }

    @Test
    void cannotReviewABookingThatIsNotYetCompleted() throws Exception {
        String familyToken = registerAndGetAccessToken("arvio.perhe3@example.com", UserRole.FAMILY_MEMBER);
        UUID profileId = createElderlyProfile(familyToken);
        UUID taskId = createTask(familyToken, profileId);
        String workerToken = registerVerifiedWorkerAndGetAccessToken("arvio.tyontekija3@example.com");
        UUID applicationId = applyAndGetApplicationId(taskId, workerToken);

        // Accept, so a CONFIRMED (not COMPLETED) booking exists.
        String bookingJson = mockMvc.perform(patch("/api/tasks/" + taskId + "/applications/" + applicationId + "/accept")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID bookingId = UUID.fromString(objectMapper.readTree(bookingJson).get("id").asText());

        CreateReviewRequest review = new CreateReviewRequest(5, "Liian aikaisin.");
        mockMvc.perform(post("/api/bookings/" + bookingId + "/review")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("BOOKING_NOT_COMPLETED"));
    }

    // --- helpers ---

    private record Fixture(String familyToken, UUID bookingId, UUID workerUserId) {
    }

    private Fixture createCompletedBooking(String familyEmail, String workerEmail) throws Exception {
        String familyToken = registerAndGetAccessToken(familyEmail, UserRole.FAMILY_MEMBER);
        UUID profileId = createElderlyProfile(familyToken);
        UUID taskId = createTask(familyToken, profileId);
        String workerToken = registerVerifiedWorkerAndGetAccessToken(workerEmail);
        UUID workerUserId = extractUserId(workerToken);
        UUID applicationId = applyAndGetApplicationId(taskId, workerToken);

        String bookingJson = mockMvc.perform(patch("/api/tasks/" + taskId + "/applications/" + applicationId + "/accept")
                        .header("Authorization", "Bearer " + familyToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UUID bookingId = UUID.fromString(objectMapper.readTree(bookingJson).get("id").asText());

        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-in")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/bookings/" + bookingId + "/check-out")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        return new Fixture(familyToken, bookingId, workerUserId);
    }

    private UUID createElderlyProfile(String familyToken) throws Exception {
        CreateElderlyProfileRequest elderlyRequest = new CreateElderlyProfileRequest(
                "Aino", "Korhonen", null, "Koulukatu 1", "Oulu", "90100", "fi", "mother", false);
        String profileJson = mockMvc.perform(post("/api/elderly-profiles")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(elderlyRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(profileJson).get("id").asText());
    }

    private UUID createTask(String familyToken, UUID profileId) throws Exception {
        CreateTaskRequest taskRequest = new CreateTaskRequest(
                profileId, TaskCategory.WALKING_COMPANION, "Ulkoilua",
                null, null, "Koulukatu 1", "Oulu",
                OffsetDateTime.now().plusDays(1), OffsetDateTime.now().plusDays(1).plusHours(1),
                new BigDecimal("20.00"));
        String taskJson = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + familyToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(objectMapper.readTree(taskJson).get("id").asText());
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
