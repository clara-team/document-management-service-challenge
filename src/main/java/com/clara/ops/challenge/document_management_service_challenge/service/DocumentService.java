package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioProperties;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentTagEntity;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentTagRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.http.Method;
import com.clara.ops.challenge.document_management_service_challenge.dto.DownloadUrlResponse;


@Service
public class DocumentService {

    private final MinioProperties minioProperties;

    private final DocumentRepository documentRepository;
    private final MinioClient minioClient;

    private final DocumentTagRepository documentTagRepository;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentTagRepository documentTagRepository,
            MinioClient minioClient,
            MinioProperties minioProperties
    ) {
        this.documentRepository = documentRepository;
        this.documentTagRepository = documentTagRepository;
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public DocumentEntity upload(
            MultipartFile file,
            String userName,
            String documentName,
            List<String> tags
    ) throws Exception {
        // VALIDATIONS
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }

        String fileName = file.getOriginalFilename();

        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        if (!documentName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Document name must end with .pdf");
        }
        UUID id = UUID.randomUUID();
        String objectName = userName + "/" + documentName;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(minioProperties.getBucket())
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType("application/pdf")
                        .build()
        );

        DocumentEntity document = new DocumentEntity();
        document.setId(id);
        document.setUserName(userName);
        document.setDocumentName(documentName);
        document.setMinioPath(objectName);
        document.setFileSize(file.getSize());
        document.setContentType(file.getContentType());
        document.setCreatedAt(LocalDateTime.now());

        DocumentEntity savedDocument = documentRepository.save(document);

        if (tags != null && !tags.isEmpty()) {
            List<DocumentTagEntity> tagEntities = tags.stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> {
                        DocumentTagEntity entity = new DocumentTagEntity();
                        entity.setDocumentId(savedDocument.getId());
                        entity.setTag(tag.trim());
                        return entity;
                    })
                    .toList();

            documentTagRepository.saveAll(tagEntities);
        }

        return savedDocument;
    }

    public Page<DocumentEntity> search(
            String userName,
            String documentName,
            String tag,
            int page,
            int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (tag != null && !tag.isBlank()) {
            List<DocumentTagEntity> tags = documentTagRepository.findByTag(tag);

            List<UUID> documentIds = tags.stream()
                    .map(DocumentTagEntity::getDocumentId)
                    .toList();

            return documentRepository.findByIdIn(documentIds, pageable);
        }

        if (userName != null && !userName.isBlank()) {
            return documentRepository.findByUserName(userName, pageable);
        }

        if (documentName != null && !documentName.isBlank()) {
            return documentRepository.findByDocumentName(documentName, pageable);
        }

        return documentRepository.findAll(pageable);
    }

    public DownloadUrlResponse generateDownloadUrl(UUID id) throws Exception {

        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(minioProperties.getBucket())
                        .object(document.getMinioPath())
                        .expiry(60 * 10) // 10 minutes
                        .build()
        );

        return new DownloadUrlResponse(url);
    }
}