package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioConfig;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.util.MinioUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MinioServiceImp implements IMinioService {

  @Autowired private MinioUtil minioUtil;
  @Autowired private MinioConfig minioConfig;

  @SneakyThrows
  @Override
  public FileResponseDTO putObject(MultipartFile multipartFile, String user, String name) {

    try {
      String fileName = multipartFile.getOriginalFilename();
      Long fileSize = multipartFile.getSize();
      String fileType = multipartFile.getContentType();
      String objectName = name + fileName.substring(fileName.lastIndexOf("."));
      String pathFile = user + "/" + objectName;

      if (!minioUtil.bucketExists(minioConfig.getBucketName()))
        minioUtil.makeBucket(minioConfig.getBucketName());

      minioUtil.putObject(minioConfig.getBucketName(), multipartFile, pathFile, fileType);

      return FileResponseDTO.builder()
          .filename(name)
          .fileSize(fileSize)
          .contentType(fileType)
          .pathFile(pathFile)
          .build();

    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, null, e);
    }
  }

  @Override
  public String getObjectUrl(String objectName) {
    return minioUtil.getObjectUrl(minioConfig.getBucketName(), objectName);
  }
}
