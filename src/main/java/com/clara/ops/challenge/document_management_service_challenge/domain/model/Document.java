package com.clara.ops.challenge.document_management_service_challenge.domain.model;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Document {
  String id;
  BigInteger userId;
  String documentName;
  List<String> tags;
  String minioPath;
  long fileSize;
  String fileType;
  Instant updatedAt;
  Instant createdAt;
  long checksum;
  String status;
}
