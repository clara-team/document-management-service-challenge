package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentEntity;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import com.clara.ops.challenge.document_management_service_challenge.dto.DownloadUrlResponse;
import java.util.UUID;

import java.util.List;


@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<DocumentEntity> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userName") String userName,
            @RequestParam("documentName") String documentName,
            @RequestParam(required = false) List<String> tags
    ) throws Exception {

        DocumentEntity response = documentService.upload(file, userName, documentName, tags);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public Page<DocumentEntity> search(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String documentName,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return documentService.search(userName, documentName, tag, page, size);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<DownloadUrlResponse> download(@PathVariable UUID id) throws Exception {
        return ResponseEntity.ok(documentService.generateDownloadUrl(id));
    }
}