package com.clara.ops.challenge.document_management_service_challenge.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResponse {
  @NotNull private String downloadUrl;
  private Long expirationTime;
}
