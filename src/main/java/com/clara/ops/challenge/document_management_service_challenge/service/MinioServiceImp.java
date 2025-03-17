package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import com.clara.ops.challenge.document_management_service_challenge.util.MinioUtil;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MinioServiceImp implements IMinioService {

  @Autowired private MinioUtil minioUtil;

  @Override
  public Boolean bucketExists(String bucketName) {
    return minioUtil.bucketExists(bucketName);
  }

  @Override
  public void bucketMake(String bucketName) {
    minioUtil.makeBucket(bucketName);
  }

  @Override
  public String getObjectUrl(String bucketName, String objectName) {
    return minioUtil.getObjectUrl(bucketName, objectName);
  }

  @Override
  public void putObject(
      String bucketName, FileInputDTO fileInputDTO, InputStream inputStream, Long partSize) {
    minioUtil.putObject(
        bucketName, inputStream, fileInputDTO.getPathFile(), fileInputDTO.getFileType(), partSize);
  }
}
