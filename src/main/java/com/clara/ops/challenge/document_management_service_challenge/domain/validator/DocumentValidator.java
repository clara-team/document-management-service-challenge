package com.clara.ops.challenge.document_management_service_challenge.domain.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * A utility class responsible for validating document files. This class ensures that the file meets
 * specific criteria such as file type, size, and non-emptiness.
 */
@Component
public class DocumentValidator {
  /**
   * Validates the provided file to ensure it meets specific criteria: - The file is not empty. -
   * The file is a PDF (content type: application/pdf). - The file size does not exceed 500MB.
   *
   * @param file the file to be validated
   * @throws IllegalArgumentException if the file is empty
   * @throws IllegalArgumentException if the file is not a PDF
   * @throws IllegalArgumentException if the file size exceeds 500MB
   */
  public void validateFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    if (!"application/pdf".equals(file.getContentType())) {
      throw new IllegalArgumentException("File is not a PDF");
    }
    if (file.getSize() > 500L * 1024 * 1024) {
      throw new IllegalArgumentException("File is too large. Max size is 500MB");
    }
  }
}
