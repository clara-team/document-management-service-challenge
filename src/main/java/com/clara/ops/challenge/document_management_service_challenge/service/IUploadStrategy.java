package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileInputDTO;
import org.springframework.web.multipart.MultipartFile;

public interface IUploadStrategy {
  void processUpload(FileInputDTO fileInputDTO, MultipartFile multipartFile);
}
