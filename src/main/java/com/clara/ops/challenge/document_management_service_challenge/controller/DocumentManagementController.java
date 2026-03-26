package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.DocumentSearchFiltersRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.exception.ValidationException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.SmartValidator;
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

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequestMapping("/document-management")
@Tag(name = "Document Management", description = "Endpoints for document management.")
@RequiredArgsConstructor
public class DocumentManagementController {

    private final DocumentService service;
    private final ObjectMapper objectMapper;
    private final SmartValidator validator;

    @PostMapping(path = "/upload", consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(operationId = "uploadDocument")
    @ResponseStatus(value = CREATED)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "The document was uploaded successfully.")
    })
    public void uploadDocument(@RequestPart("file") MultipartFile file, @RequestPart("metadata") String metadata) throws IOException {
        UploadDocumentRequest uploadDocument = convertAndValidateJson(metadata);
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
            @Schema(description = "Zero-based page index (0..N)", defaultValue = "0", minimum = "0")
            @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
            @Schema(description = "The size of the page to be returned", defaultValue = "20", minimum = "1")
            @RequestParam(name = "size", required = false, defaultValue = "20") Integer size,
            @Schema(description = "Sorting criteria in the format: property,(asc|desc). " +
                    "Default sort order is ascending. Multiple sort criteria are supported.")
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

    private UploadDocumentRequest convertAndValidateJson(String metadata) throws JsonProcessingException {
        UploadDocumentRequest uploadDocument = objectMapper.readValue(metadata, UploadDocumentRequest.class);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(uploadDocument, UploadDocumentRequest.class.getSimpleName());
        validator.validate(uploadDocument, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new ValidationException(bindingResult);
        }

        return uploadDocument;
    }

}
