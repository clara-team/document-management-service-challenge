package com.clara.ops.challenge.document_management_service_challenge.domain.validator;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class DocumentValidatorTest {

  private final DocumentValidator documentValidator = new DocumentValidator();

  @Test
  void shouldThrowExceptionWhenFileIsEmpty() {
    MultipartFile file = new MockMultipartFile("emptyFile", new byte[0]);
    assertThrows(
        IllegalArgumentException.class,
        () -> documentValidator.validateFile(file),
        "File is empty");
  }

  @Test
  void shouldThrowExceptionWhenFileIsNotPdf() {
    MultipartFile file =
        new MockMultipartFile("file", "file.txt", "text/plain", "content".getBytes());
    assertThrows(
        IllegalArgumentException.class,
        () -> documentValidator.validateFile(file),
        "File is not a PDF");
  }

  @Test
  void shouldThrowExceptionWhenFileIsTooLarge() {
    byte[] largeContent = new byte[(int) (501L * 1024 * 1024)]; // 501MB
    MultipartFile file =
        new MockMultipartFile("largeFile", "largeFile.pdf", "application/pdf", largeContent);
    assertThrows(
        IllegalArgumentException.class,
        () -> documentValidator.validateFile(file),
        "File is too large. Max size is 500MB");
  }

  @Test
  void shouldNotThrowExceptionForValidFile() {
    byte[] content = new byte[(int) (10L * 1024 * 1024)]; // 10MB
    MultipartFile file =
        new MockMultipartFile("validFile", "validFile.pdf", "application/pdf", content);
    documentValidator.validateFile(file);
  }
}
