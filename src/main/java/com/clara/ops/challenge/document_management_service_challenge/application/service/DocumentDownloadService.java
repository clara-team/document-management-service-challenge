package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import com.clara.ops.challenge.document_management_service_challenge.application.exception.DocumentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class DocumentDownloadService {

    private final int presignedUrlExpiryMinutes;
    private final DocumentRepository documentRepository;
    private final MinioStorageService storageService;

    public DocumentDownloadService(DocumentRepository documentRepository,
                                   MinioStorageService storageService,
                                   @Value("${minio.presigned-url-expiry-minutes:15}") int presignedUrlExpiryMinutes
    ) {
        this.documentRepository = documentRepository;
        this.storageService = storageService;
        this.presignedUrlExpiryMinutes = presignedUrlExpiryMinutes;
    }

    @Transactional(readOnly = true)
    public DocumentDownloadUrl getDownloadUrl(UUID documentId) {
        var entity =
                documentRepository
                        .findById(documentId)
                        .orElseThrow(() -> new DocumentNotFoundException(documentId));

        String preSignedUrl =
                storageService.generatePresignedUrl(entity.getMinioPath(), this.presignedUrlExpiryMinutes);

        log.info("Generated presigned URL for document: {}", documentId);
        return new DocumentDownloadUrl(preSignedUrl);
    }

}
