package com.clara.ops.challenge.document_management_service_challenge.domain.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DocumentValidator {
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
