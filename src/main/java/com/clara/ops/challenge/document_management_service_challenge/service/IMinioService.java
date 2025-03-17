package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileInputDTO;
import java.io.InputStream;

public interface IMinioService {

  Boolean bucketExists(String bucketName);

  void bucketMake(String bucketName);

  String getObjectUrl(String bucketName, String objectName);

  void putObject(
      String bucketName, FileInputDTO fileInputDTO, InputStream inputStream, Long partSize);
}
