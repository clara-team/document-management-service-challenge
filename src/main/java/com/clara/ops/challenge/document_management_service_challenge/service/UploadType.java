package com.clara.ops.challenge.document_management_service_challenge.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum UploadType {
  FILE_PARTITION,
  SEMAPHORE,
  DISK_UPLOAD
}
