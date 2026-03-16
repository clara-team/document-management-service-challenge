package com.clara.ops.challenge.document_management_service_challenge.web;

import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.application.dto.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentDownloadService;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentSearchService;
import com.clara.ops.challenge.document_management_service_challenge.application.service.DocumentUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/document-management")
@RequiredArgsConstructor
public class DocumentManagementController {

    private final DocumentUploadService uploadService;
    private final DocumentSearchService searchService;
    private final DocumentDownloadService downloadService;

    @PostMapping(value = "/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadDocument(HttpServletRequest request) {
        uploadService.upload(request);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedDocumentSearch> searchDocuments(
            @RequestBody(required = false) DocumentSearchFilters filters,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        DocumentSearchFilters safeFilters = filters != null ? filters : new DocumentSearchFilters();
        return ResponseEntity.ok(searchService.search(safeFilters, page, size));
    }


    @GetMapping("/download/{documentId}")
    public ResponseEntity<DocumentDownloadUrl> downloadDocument(
            @PathVariable UUID documentId) {
        return ResponseEntity.ok(downloadService.getDownloadUrl(documentId));
    }

}
