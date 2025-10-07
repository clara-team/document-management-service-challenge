package com.clara.ops.challenge.document_management_service_challenge.application.port.out;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/** Interface for handling document storage operations. */
public interface DocumentStoragePort {
  String uploadFile(MultipartFile file, String userId) throws IOException;

  String generatePresignedUrl(String objectPath);

  InputStream download(String path, String filename);

  void delete(String path);
}
