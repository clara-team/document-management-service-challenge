package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.List;
import lombok.Data;

@Data
public class DocumentSearchFilters {
  private String username;
  private String name;
  private List<String> tags;
}
