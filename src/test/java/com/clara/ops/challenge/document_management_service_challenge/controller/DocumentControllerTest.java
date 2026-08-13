package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.dto.DocumentDownloadUrlDTO;
import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DocumentService documentService;

  private Document document;

  @BeforeEach
  void setUp() {
    document = new Document();
    document.setId(1L);
    document.setUsername("Matthew");
    document.setDocumentName("imgM");
    document.setTags(Arrays.asList("tag1", "tag3"));
    document.setFileSize(12345L);
    document.setFileType("image/png");
    document.setCreatedAt(null);
  }

  @Test
  void testUploadDocument() throws Exception {
    // Create a JSON string for the metadata
    String metadataJson =
        "{\"username\":\"Matthew\",\"name\":\"imgM\",\"tags\":[\"tag1\",\"tag3\"]}";

    // Create the MockMultipartFiles for metadata and file parts.
    MockMultipartFile metadataPart =
        new MockMultipartFile(
            "metadata", // The name of the part as expected by @RequestPart("metadata")
            "metadata.json", // Filename (can be arbitrary)
            "application/json", // Content type
            metadataJson.getBytes() // Content as bytes
            );

    MockMultipartFile filePart =
        new MockMultipartFile(
            "file", // The name of the part as expected by @RequestPart("file")
            "test.txt", // Filename for the uploaded file
            "text/plain", // Content type
            "dummy content".getBytes() // File content as bytes
            );

    when(documentService.uploadDocument(anyString(), anyString(), anyList(), any()))
        .thenReturn(document);

    mockMvc
        .perform(
            multipart("/document-management/upload")
                .file(metadataPart) // dummy file content
                .file(filePart))
        .andExpect(status().isCreated());
  }

  @Test
  void testSearchDocuments() throws Exception {
    // Prepare mock data
    List<Document> documents = Arrays.asList(document);

    Page<Document> page = new PageImpl<>(documents); // Correct return type

    when(documentService.searchDocuments(any(), any(), any(), any())).thenReturn(page);

    mockMvc
        .perform(
            post("/document-management/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"Matthew\",\"name\":\"imgM\",\"tags\":[\"tag1\"]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.documents.length()").value(1))
        .andExpect(jsonPath("$.documents[0].username").value("Matthew"));
  }

  @Test
  void testDownloadDocument() throws Exception {
    String downloadURL = "http://example.com/download/1";
    DocumentDownloadUrlDTO downloadUrlDTO = new DocumentDownloadUrlDTO();
    downloadUrlDTO.setUrl(downloadURL);

    when(documentService.getDownloadUrl(anyLong())).thenReturn(downloadURL);

    mockMvc
        .perform(get("/document-management/download/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.url").value(downloadURL));
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    public DocumentService documentService() {
      // Create and return a Mockito mock for DocumentService
      return Mockito.mock(DocumentService.class);
    }
  }
}
