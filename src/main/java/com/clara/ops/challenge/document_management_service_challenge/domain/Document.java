package com.clara.ops.challenge.document_management_service_challenge.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column private String userId;

  @Column private String documentName;

  @Column private String filePath;

  @Column private Long fileSize;

  @Column private String fileType;

  @Builder.Default
  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  @Column(insertable = false)
  private LocalDateTime createdAt;
}
