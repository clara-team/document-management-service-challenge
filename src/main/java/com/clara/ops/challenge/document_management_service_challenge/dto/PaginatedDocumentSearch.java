package com.clara.ops.challenge.document_management_service_challenge.dto;

import java.util.List;
import lombok.Data;

@Data
public class PaginatedDocumentSearch {
  private Metadata metadata;
  private List<Document> documents;
}
