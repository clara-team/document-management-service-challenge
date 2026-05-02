package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.request.DocumentSearchFilter;
import com.clara.ops.challenge.document_management_service_challenge.domain.Document;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/documents")
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@NotBlank @RequestParam("name") String name,
                       @NotBlank @RequestParam("user") String user,
                       @RequestParam(value = "tags", required = false) List<String> tags,
                       @RequestPart("file") MultipartFile file) {
        var document = Document.builder()
                .userId(user)
                .documentName(name)
                .tags(tags)
                .fileType(file.getContentType())
                .fileSize(file.getSize()).build();

        documentService.upload(document, file);
    }

    @PostMapping(value = "/search")
    public Page<Document> search(@RequestBody DocumentSearchFilter filter,
                                 @RequestParam(required = false, defaultValue = "0") int page,
                                 @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return documentService.filter(filter, PageRequest.of(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(documentService.getDownloadUrlById(id));
    }
}
