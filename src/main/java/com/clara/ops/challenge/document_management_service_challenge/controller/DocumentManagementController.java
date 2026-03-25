package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocument;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document-management")
@Tag(name = "Document Management", description = "Endpoints for document management.")
public class DocumentManagementController {

    @PostMapping("/upload")
    @Operation(operationId = "uploadDocument")
    public String uploadDocument(@Valid @RequestBody UploadDocument request) {
        return "test";
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
