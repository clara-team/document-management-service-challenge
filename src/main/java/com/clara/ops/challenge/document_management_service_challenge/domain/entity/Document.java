package com.clara.ops.challenge.document_management_service_challenge.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jdk.jfr.Timestamp;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "document", schema = "document_schema")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @NotNull
    @Column(length = 200, nullable = false)
    private String username;

    @NotNull
    @Column(length = 200, nullable = false)
    private String filename;

    @NotNull
    @Column(name = "file_path", length = 200, nullable = false)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 80)
    private String fileType;

    @NotNull
    @Timestamp
    @Column(name= "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @OneToMany(mappedBy = "document")
    private Set<DocumentTag> documentTags;

}
