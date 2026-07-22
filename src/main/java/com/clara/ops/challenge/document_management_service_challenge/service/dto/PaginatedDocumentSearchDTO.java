package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaginatedDocumentSearchDTO {
  private MetadataDTO metadata;
  private List<DocumentDTO> documents;
}
