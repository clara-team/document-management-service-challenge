package com.clara.ops.challenge.document_management_service_challenge.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Document {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "document_name", nullable = false)
  private String documentName;

  @Column(name = "minio_path", nullable = false)
  private String minioPath;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "file_type", nullable = false)
  private String fileType;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "document_tags",
      joinColumns = @JoinColumn(name = "document_id"),
      inverseJoinColumns = @JoinColumn(name = "tag_id"))
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Set<Tag> tags = new HashSet<>();

  public void addTag(Tag tag) {
    this.tags.add(tag);
    tag.getDocuments().add(this);
  }

  public void removeTag(Tag tag) {
    this.tags.remove(tag);
    tag.getDocuments().remove(this);
  }
}
