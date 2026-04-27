package com.clara.ops.challenge.document_management_service_challenge.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "documents")
public class DocumentEntity {

    @Id
    private UUID id;

    private String userName;
    private String documentName;
    private String minioPath;
    private Long fileSize;
    private String contentType;
    private LocalDateTime createdAt;
}