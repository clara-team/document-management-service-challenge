package com.clara.ops.challenge.document_management_service_challenge.dto;

import lombok.Data;

@Data
public class Metadata {
  private int currentPage;
  private int itemsPerPage;
  private int currentItems;
  private int totalPages;
  private long totalItems;
}
