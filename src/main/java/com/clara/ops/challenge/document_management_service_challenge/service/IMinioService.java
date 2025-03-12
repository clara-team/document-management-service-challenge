package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IMinioService {

  FileResponseDTO putObject(MultipartFile multipartFile, String User, String name);

  String getObjectUrl(String objectName);
}
