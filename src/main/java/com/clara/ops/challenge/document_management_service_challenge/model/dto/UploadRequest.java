package com.clara.ops.challenge.document_management_service_challenge.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadRequest {
  @NotBlank(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Document name is required")
  private String documentName;

  @NotEmpty(message = "At least one tag is required")
  private List<String> tags;
}
