package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.application.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentDownloadService;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DocumentDownloadServiceTest {

    @Mock
    DocumentRepository documentRepository;
    @Mock
    MinioStorageService storageService;

    DocumentDownloadService downloadService;

    @BeforeEach
    void setUp() {
        downloadService = new DocumentDownloadService(documentRepository, storageService, 15);
    }

    @Test
    void getDownloadUrl_existingDocument_returnsPresignedUrl() {
        UUID id = UUID.randomUUID();
        var entity = DocumentEntity.builder()
                .id(id)
                .minioPath("john/doc.pdf")
                .build();

        when(documentRepository.findById(id)).thenReturn(Optional.of(entity));
        when(storageService.generatePresignedUrl("john/doc.pdf", 15))
                .thenReturn("http://minio/doc.pdf?signature=abc");

        var result = downloadService.getDownloadUrl(id);

        assertThat(result.getUrl()).contains("signature=abc");
    }

    @Test
    void getDownloadUrl_nonExistentDocument_throwsDocumentNotFoundException() {
        UUID id = UUID.randomUUID();
        when(documentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> downloadService.getDownloadUrl(id))
                .isInstanceOf(DocumentNotFoundException.class);
    }

}
