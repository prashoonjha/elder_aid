package com.elderaid.platform.worker;

import com.elderaid.platform.domain.user.AppUser;
import com.elderaid.platform.domain.user.UserRole;
import com.elderaid.platform.domain.user.UserStatus;
import com.elderaid.platform.domain.worker.VerificationTier;
import com.elderaid.platform.repository.UserRepository;
import com.elderaid.platform.repository.WorkerProfileRepository;
import com.elderaid.platform.security.JwtService;
import com.elderaid.platform.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class AdminVerificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("app.storage.upload-dir", () -> "./build/test-uploads-admin");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkerProfileRepository workerProfileRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void approvingIdAndSelfieDocumentsPromotesWorkerToTier1() throws Exception {
        RegisterRequest workerRegistration = new RegisterRequest(
                "hyvaksytty@example.com", "Hyva", "Tyontekija",
                "TurvallinenSalasana1", null, UserRole.WORKER, true, "fi");
        String registerResponseJson = mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(workerRegistration)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String workerAccessToken = objectMapper.readTree(registerResponseJson).get("accessToken").asText();

        mockMvc.perform(multipart("/api/workers/me/verification-documents")
                        .file(new MockMultipartFile("file", "id.jpg", "image/jpeg", "id-bytes".getBytes()))
                        .param("documentType", "ID_CARD")
                        .header("Authorization", "Bearer " + workerAccessToken))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/workers/me/verification-documents")
                        .file(new MockMultipartFile("file", "selfie.jpg", "image/jpeg", "selfie-bytes".getBytes()))
                        .param("documentType", "SELFIE")
                        .header("Authorization", "Bearer " + workerAccessToken))
                .andExpect(status().isCreated());

        // Admin accounts are seeded directly, never self-registered - this
        // mirrors how that account would actually come to exist.
        String adminAccessToken = seedAdminAndGetAccessToken("admin@example.com");

        String pendingJson = mockMvc.perform(get("/api/admin/verification-documents")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andReturn().getResponse().getContentAsString();

        var pendingDocs = objectMapper.readTree(pendingJson);
        for (var doc : pendingDocs) {
            UUID documentId = UUID.fromString(doc.get("id").asText());
            mockMvc.perform(post("/api/admin/verification-documents/" + documentId + "/approve")
                            .header("Authorization", "Bearer " + adminAccessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        UUID workerUserId = extractUserId(workerAccessToken);
        var workerProfile = workerProfileRepository.findByUserId(workerUserId).orElseThrow();
        assertThat(workerProfile.getVerificationTier()).isEqualTo(VerificationTier.TIER1_ID_VERIFIED);
    }

    private String seedAdminAndGetAccessToken(String email) {
        AppUser admin = AppUser.builder()
                .email(email)
                .firstName("Admin")
                .lastName("User")
                .passwordHash("not-used-in-this-test")
                .locale("fi")
                .status(UserStatus.ACTIVE)
                .roles(new HashSet<>(java.util.List.of(UserRole.ADMIN)))
                .build();
        admin = userRepository.save(admin);
        return jwtService.generateAccessToken(admin);
    }

    private UUID extractUserId(String accessToken) {
        String[] parts = accessToken.split("\\.");
        byte[] payloadBytes = java.util.Base64.getUrlDecoder().decode(parts[1]);
        try {
            return UUID.fromString(objectMapper.readTree(payloadBytes).get("sub").asText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
