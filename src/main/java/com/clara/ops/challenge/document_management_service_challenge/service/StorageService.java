package com.clara.ops.challenge.document_management_service_challenge.service;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

  String uploadFile(String userId, String documentName, MultipartFile file);

  String generatePresignedUrl(String objectPath);
}
