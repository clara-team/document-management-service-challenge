package com.clara.ops.challenge.document_management_service_challenge.web;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.UploadDocumentRequestDto;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
public class DocumentManagementController {

    private final DocumentUploadService uploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadDocument(
            @RequestPart("file") MultipartFile file,
            @Valid @ModelAttribute UploadDocumentRequestDto request) {
        uploadService.upload(file, request);
    }

}
