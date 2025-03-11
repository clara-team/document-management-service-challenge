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
public class SearchResponse {
  private List<DocumentInfo> documents;
  private int currentPage;
  private long totalItems;
  private int totalPages;
}
