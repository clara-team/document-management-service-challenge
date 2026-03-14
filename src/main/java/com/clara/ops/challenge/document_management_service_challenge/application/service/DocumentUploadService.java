package com.clara.ops.challenge.document_management_service_challenge.application.service;

import java.io.IOException;
import java.io.InputStream;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.UploadDocumentRequestDto;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final MinioStorageService storageService;
    private final DocumentRepository documentRepository;

    @Transactional
    public void upload(MultipartFile file, UploadDocumentRequestDto request) {
        validateFile(file);

        String objectPath = buildObjectPath(request.getUser(), request.getName());

        try (InputStream inputStream = file.getInputStream()) {
            storageService.upload(objectPath, inputStream, PDF_CONTENT_TYPE);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read uploaded file", e);
        }

        DocumentEntity entity =
                DocumentEntity.builder()
                        .userName(request.getUser())
                        .name(request.getName())
                        .minioPath(objectPath)
                        .fileSize(file.getSize())
                        .fileType(PDF_CONTENT_TYPE)
                        .tags(request.getTags())
                        .build();

        documentRepository.save(entity);
        log.info("Document saved: path={}, user={}", objectPath, request.getUser());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        String contentType = file.getContentType();
        if (!PDF_CONTENT_TYPE.equals(contentType)) {
            throw new IllegalArgumentException("Only PDF files are accepted");
        }
    }

    private String buildObjectPath(String userName, String documentName) {
        String safeUser = userName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String safeName = documentName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safeUser + "/" + safeName;
    }

}
