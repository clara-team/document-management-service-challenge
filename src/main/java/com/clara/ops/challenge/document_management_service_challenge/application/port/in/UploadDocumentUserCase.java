package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.DocumentMetadata;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto.UploadDocumentResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface for managing the use case of uploading a document. Provides functionality to upload a
 * document file along with its associated metadata.
 */
public interface UploadDocumentUserCase {
  UploadDocumentResponse upload(MultipartFile file, DocumentMetadata metadata) throws IOException;
}
