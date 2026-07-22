package com.clara.ops.challenge.document_management_service_challenge.application.port.in;

import java.util.UUID;
import org.springframework.core.io.InputStreamResource;

public interface DownloadDocumentUserCase {
  InputStreamResource downloadFile(UUID documentId);
}
