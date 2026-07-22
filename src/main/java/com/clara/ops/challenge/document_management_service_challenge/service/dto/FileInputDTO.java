package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class FileInputDTO {
  private String nameDocument;
  private String pathFile;
  private String fileType;
  private Long fileSize;
}
