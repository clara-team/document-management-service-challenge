package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class Document {
  private String id;
  private String username;
  private String name;
  private List<String> tags;
  private Long size;
  private String type;
  private LocalDateTime createdAt;
}
