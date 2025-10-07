package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.web;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.clara.ops.challenge.document_management_service_challenge.application.port.in.DownloadDocumentUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.port.in.FindDocumentMetadataUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.service.UploadDocumentService;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentValidationException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import java.io.ByteArrayInputStream;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DocumentManagementControllerTest {

  private MockMvc mockMvc;
  private UploadDocumentService uploadServiceMock;
  private DownloadDocumentUserCase downloadDocumentUserCaseMock;
  private FindDocumentMetadataUserCase findDocumentMetadataUserCaseMock;

  private MockMultipartFile mockFile;
  private MockMultipartFile mockMetadata;

  @BeforeEach
  void setup() {
    uploadServiceMock = Mockito.mock(UploadDocumentService.class);
    downloadDocumentUserCaseMock = Mockito.mock(DownloadDocumentUserCase.class);
    findDocumentMetadataUserCaseMock = Mockito.mock(FindDocumentMetadataUserCase.class);

    DocumentManagementController controller =
        new DocumentManagementController(
            uploadServiceMock, downloadDocumentUserCaseMock, findDocumentMetadataUserCaseMock);
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    mockFile =
        new MockMultipartFile("file", "test.pdf", "application/pdf", "fake-pdf-content".getBytes());
    mockMetadata =
        new MockMultipartFile(
            "metadata",
            "",
            "application/json",
            """
            {"tags":["unit","test"],"userId":"user123"}
            """
                .getBytes());
  }

  @Test
  @DisplayName("Should succeed and returns a 201 with documentId")
  void whenUploadValidDocument_thenReturn201() throws Exception {
    UUID id = UUID.randomUUID();
    Mockito.when(uploadServiceMock.upload(any(), any())).thenReturn(new UploadDocumentResponse(id));

    mockMvc
        .perform(
            multipart("/api/v1/document-management/upload")
                .file(mockFile)
                .file(mockMetadata)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.documentId").value(id.toString()));
  }

  @Test
  @DisplayName("Should return 409 Document already exists")
  void whenDuplicateDocument_thenReturn409() throws Exception {
    Mockito.when(uploadServiceMock.upload(any(), any()))
        .thenThrow(new DocumentValidationException("Document already exists", "1", "test.pdf"));

    mockMvc
        .perform(
            multipart("/api/v1/document-management/upload")
                .file(mockFile)
                .file(mockMetadata)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.message").value("Document already exists (userId=1, fileName=test.pdf)"));
  }

  @Test
  @DisplayName("Upload document should throws 400")
  void whenMissingMetadata_thenReturn400() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/document-management/upload")
                .file(mockFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MISSING_REQUEST_PART"));
  }

  @Test
  @DisplayName("Upload document should throws Unexpected error ")
  void whenServerError_thenReturn500() throws Exception {
    Mockito.when(uploadServiceMock.upload(any(), any()))
        .thenThrow(new RuntimeException("Internal server error"));

    mockMvc
        .perform(
            multipart("/api/v1/document-management/upload")
                .file(mockFile)
                .file(mockMetadata)
                .contentType(MediaType.MULTIPART_FORM_DATA))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("Internal server error"));
  }

  @Test
  @DisplayName("Download should return 200 and PDF file with correct headers")
  void whenDownloadValidDocument_thenReturn200() throws Exception {
    UUID id = UUID.randomUUID();
    byte[] pdfBytes = "fake-pdf-content".getBytes();

    Document document =
        Document.builder().id(id).documentName("test.pdf").fileSize(pdfBytes.length).build();

    InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(pdfBytes));

    Mockito.when(findDocumentMetadataUserCaseMock.findMetadata(id)).thenReturn(document);
    Mockito.when(downloadDocumentUserCaseMock.downloadFile(id)).thenReturn(resource);

    mockMvc
        .perform(get("/api/v1/document-management/{id}/download", id))
        .andExpect(status().isOk())
        .andExpect(
            header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test.pdf\""))
        .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"))
        .andExpect(content().bytes(pdfBytes));
  }

  @Test
  @DisplayName("Download should return 404 when document not found")
  void whenDownloadDocumentNotFound_thenReturn404() throws Exception {
    UUID id = UUID.randomUUID();

    Mockito.when(findDocumentMetadataUserCaseMock.findMetadata(id))
        .thenThrow(new DocumentValidationException("Document not found", id.toString(), "N/A"));

    mockMvc
        .perform(get("/api/v1/document-management/{id}/download", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(
            jsonPath("$.message").value("Document not found (userId=" + id + ", fileName=N/A)"));
  }

  @Test
  @DisplayName("Download should return 500 on unexpected error")
  void whenDownloadUnexpectedError_thenReturn500() throws Exception {
    UUID id = UUID.randomUUID();

    Mockito.when(findDocumentMetadataUserCaseMock.findMetadata(id))
        .thenThrow(new RuntimeException("Unexpected error"));

    mockMvc
        .perform(get("/api/v1/document-management/{id}/download", id))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
        .andExpect(jsonPath("$.message").value("Unexpected error"));
  }
}
