package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.model.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentController.class)
public class DocumentControllerTest {

  @Autowired private MockMvc mockMvc;

  // I just realized today that MockBean is deprecated for SpringBoot 3.4 but I just didnt have time
  // to change it.
  // Anyways the tests are working properly and with more time I can find the workaround
  @MockBean private DocumentService documentService;

  @Autowired private ObjectMapper objectMapper;

  private DocumentInfo sampleDocumentInfo;
  private SearchResponse sampleSearchResponse;
  private DownloadResponse sampleDownloadResponse;
  private MockMultipartFile validPdfFile;
  private MockMultipartFile invalidFile;
  private MockMultipartFile largeFile;

  @BeforeEach
  void setUp() {
    // Setup sample document info
    sampleDocumentInfo =
        DocumentInfo.builder()
            .id(1L)
            .userId("test-user")
            .documentName("test-document.pdf")
            .tags(Arrays.asList("tag1", "tag2"))
            .fileSize(1024L)
            .fileType("application/pdf")
            .createdAt(LocalDateTime.now())
            .build();

    // Setup sample search response
    sampleSearchResponse =
        SearchResponse.builder()
            .documents(Collections.singletonList(sampleDocumentInfo))
            .currentPage(0)
            .totalItems(1)
            .totalPages(1)
            .build();

    // Setup sample download response
    sampleDownloadResponse =
        DownloadResponse.builder()
            .downloadUrl("https://minio-endpoint/test-bucket/test-user/test-document.pdf?token=xyz")
            .expirationTime(System.currentTimeMillis() + 30 * 60 * 1000) // 30 minutes from now
            .build();

    // Setup mock files
    validPdfFile =
        new MockMultipartFile(
            "file", "test-document.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF content".getBytes());

    invalidFile =
        new MockMultipartFile(
            "file",
            "test-document.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "Plain text content".getBytes());

    largeFile =
        new MockMultipartFile(
            "file",
            "large-document.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "PDF content".getBytes()) {
          @Override
          public long getSize() {
            return 501 * 1024 * 1024; // 501 MB (exceeds 500 MB limit)
          }
        };
  }

  @Test
  void uploadDocument_Success() throws Exception {
    // Setup mock service behavior
    when(documentService.uploadDocument(any(UploadRequest.class), any()))
        .thenReturn(sampleDocumentInfo);

    // Execute and assert
    mockMvc
        .perform(
            multipart("/api/v1/document/upload")
                .file(validPdfFile)
                .param("user", "test-user")
                .param("document_name", "test-document.pdf")
                .param("tags", "tag1", "tag2"))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id", is(1)))
        .andExpect(jsonPath("$.userId", is("test-user")))
        .andExpect(jsonPath("$.documentName", is("test-document.pdf")))
        .andExpect(jsonPath("$.tags", hasSize(2)))
        .andExpect(jsonPath("$.tags[0]", is("tag1")))
        .andExpect(jsonPath("$.tags[1]", is("tag2")))
        .andExpect(jsonPath("$.fileSize", is(1024)));
  }

  @Test
  void uploadDocument_InvalidFileType() throws Exception {
    // Execute and assert
    mockMvc
        .perform(
            multipart("/api/v1/document/upload")
                .file(invalidFile)
                .param("user", "test-user")
                .param("document_name", "test-document.txt")
                .param("tags", "tag1", "tag2"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void uploadDocument_FileTooLarge() throws Exception {
    // Execute and assert
    mockMvc
        .perform(
            multipart("/api/v1/document/upload")
                .file(largeFile)
                .param("user", "test-user")
                .param("document_name", "large-document.pdf")
                .param("tags", "tag1", "tag2"))
        .andExpect(status().isPayloadTooLarge());
  }

  @Test
  void searchDocuments_Success() throws Exception {
    // Setup mock service behavior
    when(documentService.searchDocuments(any(SearchRequest.class)))
        .thenReturn(sampleSearchResponse);

    // Execute and assert
    mockMvc
        .perform(
            get("/api/v1/document/search")
                .param("userId", "test-user")
                .param("documentName", "test")
                .param("tags", "tag1", "tag2")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.documents", hasSize(1)))
        .andExpect(jsonPath("$.documents[0].id", is(1)))
        .andExpect(jsonPath("$.documents[0].userId", is("test-user")))
        .andExpect(jsonPath("$.documents[0].documentName", is("test-document.pdf")))
        .andExpect(jsonPath("$.currentPage", is(0)))
        .andExpect(jsonPath("$.totalItems", is(1)))
        .andExpect(jsonPath("$.totalPages", is(1)));
  }

  @Test
  void searchDocuments_NoFilters() throws Exception {
    // Setup mock service behavior
    when(documentService.searchDocuments(any(SearchRequest.class)))
        .thenReturn(sampleSearchResponse);

    // Execute and assert
    mockMvc
        .perform(get("/api/v1/document/search"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.documents", hasSize(1)))
        .andExpect(jsonPath("$.documents[0].id", is(1)));
  }

  @Test
  void getDocumentDownloadUrl_Success() throws Exception {
    // Setup mock service behavior
    when(documentService.getDocumentDownloadUrl(anyLong())).thenReturn(sampleDownloadResponse);

    // Execute and assert
    mockMvc
        .perform(get("/api/v1/document/1/download"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(
            jsonPath(
                "$.downloadUrl",
                is("https://minio-endpoint/test-bucket/test-user/test-document.pdf?token=xyz")))
        .andExpect(jsonPath("$.expirationTime").exists());
  }

  @Test
  void getDocumentDownloadUrl_DocumentNotFound() throws Exception {
    // Setup mock service behavior
    when(documentService.getDocumentDownloadUrl(eq(999L)))
        .thenThrow(new DocumentNotFoundException("Document not found with ID: 999"));

    // Execute and assert
    mockMvc.perform(get("/api/v1/document/999/download")).andExpect(status().isNotFound());
  }
}
