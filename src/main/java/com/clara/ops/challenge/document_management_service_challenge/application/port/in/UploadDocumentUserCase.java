package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadDocumentUserCase {
  UploadDocumentResponse upload(MultipartFile file, DocumentMetadata metadata, String userId)
      throws IOException;
}
