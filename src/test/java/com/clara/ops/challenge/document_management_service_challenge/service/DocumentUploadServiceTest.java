package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentUploadService;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {

  @Mock MinioStorageService storageService;
  @Mock DocumentRepository documentRepository;
  @InjectMocks DocumentUploadService uploadService;

  // ── Helpers ───────────────────────────────────────────────────────────────

  private MockHttpServletRequest buildRequest(
      String user, String name, String[] tags, byte[] fileContent, String fileContentType)
      throws IOException {
    String boundary = "TestBoundary123";
    ByteArrayOutputStream body = new ByteArrayOutputStream();

    if (user != null) writeField(body, boundary, "user", user);
    if (name != null) writeField(body, boundary, "name", name);
    if (tags != null) {
      for (String tag : tags) writeField(body, boundary, "tags", tag);
    }
    if (fileContent != null) {
      body.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
      body.write(
          ("Content-Disposition: form-data; name=\"file\"; filename=\"test.pdf\"\r\n")
              .getBytes(StandardCharsets.UTF_8));
      body.write(
          ("Content-Type: " + fileContentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
      body.write(fileContent);
      body.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
    body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setContentType("multipart/form-data; boundary=" + boundary);
    request.setContent(body.toByteArray());
    return request;
  }

  private void writeField(ByteArrayOutputStream out, String boundary, String name, String value)
      throws IOException {
    out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
    out.write(
        ("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
            .getBytes(StandardCharsets.UTF_8));
    out.write((value + "\r\n").getBytes(StandardCharsets.UTF_8));
  }

  // ── Tests ─────────────────────────────────────────────────────────────────

  @Test
  void upload_validPdf_savesEntityAndCallsStorage() throws IOException {
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MockHttpServletRequest request =
        buildRequest("john", "doc.pdf", new String[] {"legal"}, new byte[] {1, 2, 3}, "application/pdf");

    uploadService.upload(request);

    verify(storageService).upload(eq("john/doc.pdf"), any(InputStream.class), eq("application/pdf"));
    verify(documentRepository)
        .save(
            argThat(
                e ->
                    e.getUserName().equals("john")
                        && e.getName().equals("doc.pdf")
                        && e.getTags().contains("legal")));
  }

  @Test
  void upload_missingUser_throwsIllegalArgumentException() throws IOException {
    MockHttpServletRequest request =
        buildRequest(null, "doc.pdf", new String[] {"legal"}, new byte[] {1}, "application/pdf");

    assertThatThrownBy(() -> uploadService.upload(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("user");
  }

  @Test
  void upload_nonPdfFile_throwsIllegalArgumentException() throws IOException {
    MockHttpServletRequest request =
        buildRequest("u", "doc.pdf", new String[] {"t"}, new byte[] {1}, "image/png");

    assertThatThrownBy(() -> uploadService.upload(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("PDF");
  }

  @Test
  void upload_pathTraversalInUserName_isSanitized() throws IOException {
    when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    MockHttpServletRequest request =
        buildRequest(
            "../admin", "doc.pdf", new String[] {"t"}, new byte[] {1, 2, 3}, "application/pdf");

    uploadService.upload(request);

    verify(storageService).upload(argThat(path -> !path.contains("..")), any(), any());
  }
}
