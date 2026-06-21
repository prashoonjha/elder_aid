package com.elderaid.platform.worker;

import com.elderaid.platform.domain.user.UserRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class WorkerVerificationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void overrideDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Keep test uploads out of the real working directory.
        registry.add("app.storage.upload-dir", () -> "./build/test-uploads");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void workerUploadsIdCardAndSeesItInOwnList() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "tarkistettava@example.com", "Liisa", "Tarkistettava",
                "TurvallinenSalasana1", null, UserRole.WORKER, true, "fi");

        String registerResponseJson = mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(registerResponseJson).get("accessToken").asText();

        MockMultipartFile idCard = new MockMultipartFile(
                "file", "id-card.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        mockMvc.perform(multipart("/api/workers/me/verification-documents")
                        .file(idCard)
                        .param("documentType", "ID_CARD")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").value("ID_CARD"))
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(get("/api/workers/me/verification-documents")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].documentType").value("ID_CARD"));
    }
}
