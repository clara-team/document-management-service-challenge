package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.request.DocumentSearchFilter;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockitoBean
  private DocumentService documentService;

  @Test
  @DisplayName("upload: returns 201 when file is uploaded successfully")
  void uploadReturns201OnSuccess() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/documents/upload")
                .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]))
                .param("name", "test-doc")
                .param("user", "alice"))
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("upload: builds Document with name, user, fileType and fileSize from request params")
  void uploadBuildsDocumentWithCorrectFieldsFromParams() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/documents/upload")
                .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[100]))
                .param("name", "test-doc")
                .param("user", "alice"))
        .andExpect(status().isCreated());

    ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
    verify(documentService).upload(captor.capture(), any());
    Document document = captor.getValue();
    assertThat(document.getDocumentName()).isEqualTo("test-doc");
    assertThat(document.getUserId()).isEqualTo("alice");
    assertThat(document.getFileType()).isEqualTo("application/pdf");
    assertThat(document.getFileSize()).isEqualTo(100L);
  }

  @Test
  @DisplayName("upload: includes tags in Document when tags param is provided")
  void uploadIncludesTagsWhenProvided() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/documents/upload")
                .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]))
                .param("name", "test-doc")
                .param("user", "alice")
                .param("tags", "finance", "2024"))
        .andExpect(status().isCreated());

    ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
    verify(documentService).upload(captor.capture(), any());
    assertThat(captor.getValue().getTags()).containsExactlyInAnyOrder("finance", "2024");
  }

  @Test
  @DisplayName("upload: returns 400 when name param is missing")
  void uploadReturns400WhenNameParamMissing() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/documents/upload")
                .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]))
                .param("user", "alice"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("upload: returns 400 when user param is missing")
  void uploadReturns400WhenUserParamMissing() throws Exception {
    mockMvc
        .perform(
            multipart("/api/v1/documents/upload")
                .file(new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]))
                .param("name", "test-doc"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("search: returns 200 with page of matching documents")
  void searchReturns200WithPageOfDocuments() throws Exception {
    Document doc = Document.builder().userId("alice").documentName("invoice").build();
    when(documentService.filter(any(), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of(doc)));

    mockMvc
        .perform(
            post("/api/v1/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new DocumentSearchFilter("alice", "invoice", null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].documentName").value("invoice"))
        .andExpect(jsonPath("$.content[0].userId").value("alice"))
        .andExpect(jsonPath("$.totalElements").value(1));
  }

  @Test
  @DisplayName("search: returns empty page when no documents match the filter")
  void searchReturnsEmptyPage() throws Exception {
    when(documentService.filter(any(), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(
            post("/api/v1/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new DocumentSearchFilter(null, null, null))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("search: uses default page 0 and size 10 when pagination params are not provided")
  void searchUsesDefaultPaginationWhenParamsNotProvided() throws Exception {
    when(documentService.filter(any(), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(List.of()));

    mockMvc
        .perform(
            post("/api/v1/documents/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(new DocumentSearchFilter(null, null, null))))
        .andExpect(status().isOk());

    ArgumentCaptor<PageRequest> captor = ArgumentCaptor.forClass(PageRequest.class);
    verify(documentService).filter(any(), captor.capture());
    assertThat(captor.getValue().getPageNumber()).isZero();
    assertThat(captor.getValue().getPageSize()).isEqualTo(10);
  }

  @Test
  @DisplayName("getDocument: returns 200 with DocumentResponse body")
  void getDocumentReturns200WithDocumentResponse() throws Exception {
    when(documentService.getDownloadUrlById(1L))
        .thenReturn(new DocumentResponse(1L, "report", "https://minio/report.pdf"));

    mockMvc
        .perform(get("/api/v1/documents/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.documentId").value(1))
        .andExpect(jsonPath("$.documentName").value("report"))
        .andExpect(jsonPath("$.url").value("https://minio/report.pdf"));
  }

  @Test
  @DisplayName("getDocument: delegates to service with the id from the path variable")
  void getDocumentDelegatesToServiceWithCorrectId() throws Exception {
    when(documentService.getDownloadUrlById(42L))
        .thenReturn(new DocumentResponse(42L, "doc", "https://minio/doc.pdf"));

    mockMvc.perform(get("/api/v1/documents/42")).andExpect(status().isOk());

    verify(documentService).getDownloadUrlById(42L);
  }
}
