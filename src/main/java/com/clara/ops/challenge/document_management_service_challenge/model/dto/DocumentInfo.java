package com.clara.ops.challenge.document_management_service_challenge.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentInfo {
  private Long id;
  private String userId;
  private String documentName;
  private List<String> tags;
  private Long fileSize;
  private String fileType;
  private LocalDateTime createdAt;
}
