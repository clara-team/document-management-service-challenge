package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.UploadDocumentRequestDto;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentUploadService;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.storage.MinioStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {

    @Mock
    MinioStorageService storageService;
    @Mock
    DocumentRepository documentRepository;
    @InjectMocks
    DocumentUploadService uploadService;

    @Test
    void upload_validPdf_savesEntityAndCallsStorage() throws IOException {
        // Arrange
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());

        var request = new UploadDocumentRequestDto("john", "doc.pdf", List.of("legal"));

        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        uploadService.upload(file, request);

        // Assert
        verify(storageService).upload(eq("john/doc.pdf"), any(InputStream.class), eq("application/pdf"));
        verify(documentRepository).save(argThat(e ->
                e.getUserName().equals("john") &&
                        e.getName().equals("doc.pdf") &&
                        e.getTags().contains("legal")
        ));
    }

    @Test
    void upload_emptyFile_throwsIllegalArgumentException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> uploadService.upload(file, new UploadDocumentRequestDto("u", "n", List.of("t"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void upload_nonPdfFile_throwsIllegalArgumentException() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/png");

        assertThatThrownBy(() -> uploadService.upload(file,
                new UploadDocumentRequestDto("u", "n", List.of("t"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PDF");
    }

    @Test
    void upload_pathTraversalInUserName_isSanitized() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(1L);
        when(file.getInputStream()).thenReturn(InputStream.nullInputStream());
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        uploadService.upload(file, new UploadDocumentRequestDto("../admin", "doc.pdf", List.of("t")));

        verify(storageService).upload(
                argThat(path -> !path.contains("..")),
                any(), any());
    }
}
