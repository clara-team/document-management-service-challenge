package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.DocumentSearchFiltersRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.service.DocumentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/document-management")
@Tag(name = "Document Management", description = "Endpoints for document management.")
@RequiredArgsConstructor
public class DocumentManagementController {

    private final DocumentService service;
    private final ObjectMapper objectMapper;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "uploadDocument")
    @ResponseStatus(value = CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The document was uploaded successfully.")
    })
    public void uploadDocument(@RequestPart("file") MultipartFile file, @RequestPart("metadata") String metadata) throws JsonProcessingException {
        // TODO deve ser testado com arquivos gigantes de 500 mb
        UploadDocumentRequest uploadDocument = objectMapper.readValue(metadata, UploadDocumentRequest.class);
        service.uploadDocument(file, uploadDocument);
    }

    @PostMapping("/search")
    @Operation(operationId = "searchDocuments")
    @ResponseStatus(value = OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The documents were found successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaginatedDocumentSearchResponse.class)
                    ))
    })
    public PaginatedDocumentSearchResponse searchDocument(
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
            @RequestParam(name = "sort", required = false) List<String> sort,
            @RequestBody DocumentSearchFiltersRequest request
    ) {
        return service.searchDocuments(page, size, sort, request);
    }

    @GetMapping("/download/{documentId}")
    @Operation(operationId = "downloadDocument")
    @ResponseStatus(value = OK)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DocumentDownloadUrlResponse.class)
                    ))
    })
    public DocumentDownloadUrlResponse download(@PathVariable Integer documentId) {
        return service.getDocumentDownloadUrl(documentId);
    }

}
