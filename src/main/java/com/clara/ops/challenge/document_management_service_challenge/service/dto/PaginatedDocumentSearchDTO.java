package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Document;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Metadata;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class PaginatedDocumentSearchDTO {
  private Metadata metadata;
  private List<Document> documents;
}
