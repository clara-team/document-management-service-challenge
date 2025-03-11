package com.clara.ops.challenge.document_management_service_challenge.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
  private String userId;
  private String documentName;
  private List<String> tags;
  private Integer page;
  private Integer size;
}
