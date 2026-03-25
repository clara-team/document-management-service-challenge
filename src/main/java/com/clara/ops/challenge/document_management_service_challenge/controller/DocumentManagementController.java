package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocument;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/document-management")
@Tag(name = "Document Management", description = "Endpoints for document management.")
@RequiredArgsConstructor
public class DocumentManagementController {

    private final DocumentService service;

    @PostMapping("/upload")
    @Operation(operationId = "uploadDocument")
    @ResponseStatus(CREATED)
    public void uploadDocument(@Valid @RequestBody UploadDocument uploadDocument) {
        service.uploadDocument(uploadDocument);
    }

    @PostMapping("/search")
    @Operation(operationId = "searchDocuments")
    public String searchDocument(@RequestBody DocumentSearchFilters request) {
        return "test";
    }

    @GetMapping("/download/{documentId}")
    @Operation(operationId = "downloadDocument")
    public String download(@PathVariable String documentId) {
        return "test";
    }

}
