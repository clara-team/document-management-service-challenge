package com.clara.ops.challenge.document_management_service_challenge.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String username;

  private String documentName;

  @ElementCollection
  @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
  @Column(name = "tag")
  private List<String> tags;

  private String minioPath;

  private Long fileSize;

  private String fileType;

  private LocalDateTime createdAt;
}
