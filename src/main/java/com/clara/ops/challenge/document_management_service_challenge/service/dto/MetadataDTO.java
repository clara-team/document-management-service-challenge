package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MetadataDTO {
  private Integer currentPage;
  private Integer itemsPerPage;
  private Integer currentItems;
  private Integer totalPages;
  private Integer totalItems;
}
