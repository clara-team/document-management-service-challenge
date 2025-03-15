package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document", schema = "document_schema")
public class Document {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  private String name;

  @ElementCollection
  @CollectionTable(
      schema = "document_schema",
      name = "document_tags",
      joinColumns = @JoinColumn(name = "document_id"))
  @Column(name = "tag")
  private List<String> tags;

  private String path;
  private Long size;
  private String type;
  private LocalDateTime createdAt;
}
