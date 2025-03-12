package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class FileResponseDTO {
  private String filename;
  private String contentType;
  private Long fileSize;
  private String pathFile;
}
