package com.clara.ops.challenge.document_management_service_challenge.service.dto;

import com.clara.ops.challenge.document_management_service_challenge.service.UploadType;
import java.util.List;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UploadDocumentDTO {
  private String user;
  private String name;
  private List<String> tags;
  private UploadType typeUpload;
  private MultipartFile file;
}
