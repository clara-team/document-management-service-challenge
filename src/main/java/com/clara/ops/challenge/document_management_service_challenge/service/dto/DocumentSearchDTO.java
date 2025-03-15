package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentSearchDTO {
  private Integer page;
  private Integer size;
  private String sort;
  private String user;
  private String name;
  private List<String> tags;
}
