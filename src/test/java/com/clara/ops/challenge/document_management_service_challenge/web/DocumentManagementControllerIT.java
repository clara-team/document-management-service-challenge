package com.clara.ops.challenge.document_management_service_challenge.web;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class DocumentManagementControllerIT {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("challenge")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("schema-init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("DB_URL", postgres::getJdbcUrl);
        registry.add("DB_USERNAME", postgres::getUsername);
        registry.add("DB_PASSWORD", postgres::getPassword);
        registry.add("MINIO_ENDPOINT", () -> "http://localhost:9000");
        registry.add("MINIO_ACCESS_KEY", () -> "test-key");
        registry.add("MINIO_SECRET_KEY", () -> "test-secret");
    }

    @MockitoBean
    MinioStorageService minioStorageService;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
    }

    //Upload

    @Test
    void upload_validRequest_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/document-management/upload")
                        .file(file)
                        .param("user", "john")
                        .param("name", "test.pdf")
                        .param("tags", "legal")
                        .param("tags", "2024"))
                .andExpect(status().isCreated());

        verify(minioStorageService).upload(eq("john/test.pdf"), any(), eq("application/pdf"));
        assertThat(documentRepository.findAll()).hasSize(1);
    }

    @Test
    void upload_missingUser_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[]{1});

        mockMvc.perform(multipart("/document-management/upload")
                        .file(file)
                        .param("name", "test.pdf")
                        .param("tags", "legal"))
                .andExpect(status().isBadRequest());
    }

    //Search

    @Test
    void search_noFilters_returnsAllDocuments() throws Exception {
        documentRepository.save(buildEntity("john", "doc1.pdf", List.of("legal")));
        documentRepository.save(buildEntity("jane", "doc2.pdf", List.of("finance")));

        mockMvc.perform(post("/document-management/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.totalItems").value(2))
                .andExpect(jsonPath("$.documents", hasSize(2)));
    }

    @Test
    void search_filterByUser_returnsOnlyMatchingDocuments() throws Exception {
        documentRepository.save(buildEntity("john", "doc1.pdf", List.of("legal")));
        documentRepository.save(buildEntity("jane", "doc2.pdf", List.of("finance")));

        mockMvc.perform(post("/document-management/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\": \"john\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metadata.totalItems").value(1))
                .andExpect(jsonPath("$.documents[0].user").value("john"));
    }

    //Download

    @Test
    void download_existingDocument_returnsPresignedUrl() throws Exception {
        var entity = documentRepository.save(buildEntity("john", "doc.pdf", List.of()));
        when(minioStorageService.generatePresignedUrl(any(), anyInt()))
                .thenReturn("http://minio/presigned");

        mockMvc.perform(get("/document-management/download/" + entity.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://minio/presigned"));
    }

    @Test
    void download_nonExistentDocument_returns404() throws Exception {
        mockMvc.perform(get("/document-management/download/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_invalidUUID_returns400() throws Exception {
        mockMvc.perform(get("/document-management/download/not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    private DocumentEntity buildEntity(String user, String name, List<String> tags) {
        return DocumentEntity.builder()
                .userName(user)
                .name(name)
                .minioPath(user + "/" + name)
                .fileSize(1024L)
                .fileType("application/pdf")
                .tags(tags)
                .build();
    }

}
