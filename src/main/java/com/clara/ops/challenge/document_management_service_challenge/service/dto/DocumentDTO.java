package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DocumentDTO {
  private String id;
  private String user;
  private String name;
  private List<String> tags;
  private Integer size;
  private String type;
  private String createdAt;
}
