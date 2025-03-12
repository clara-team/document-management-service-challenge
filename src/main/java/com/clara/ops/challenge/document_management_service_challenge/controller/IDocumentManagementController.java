package com.clara.ops.challenge.document_management_service_challenge.controller;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Document;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.PaginatedDocumentSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface IDocumentManagementController {

  @Operation(
      summary = "",
      description =
          "Allow downloading a document using its ID. Return a temporary download URL that enables"
              + " secure access to the document stored in MinIO.",
      tags = {"Document Management"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "*/*",
                    schema = @Schema(implementation = DocumentDownloadUrl.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class)))
      })
  @RequestMapping(
      value = "/document-management/download/{documentId}",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.GET)
  ResponseEntity<DocumentDownloadUrl> downloadDocument(
      @Parameter(in = ParameterIn.PATH, description = "", required = true, schema = @Schema())
          @PathVariable("documentId")
          String documentId);

  @Operation(
      summary = "",
      description = "Allow querying documents with optional filters.",
      tags = {"Document Management"})
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "The documents were found successfully.",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PaginatedDocumentSearch.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class)))
      })
  @RequestMapping(
      value = "/document-management/search",
      produces = {MediaType.APPLICATION_JSON_VALUE},
      consumes = {MediaType.APPLICATION_JSON_VALUE},
      method = RequestMethod.POST)
  ResponseEntity<PaginatedDocumentSearch> searchDocuments(
      @Parameter(in = ParameterIn.DEFAULT, description = "", required = true, schema = @Schema())
          @RequestBody
          DocumentSearchFilters filter,
      @Parameter(
              in = ParameterIn.QUERY,
              description = "Zero-based page index (0..N)",
              schema = @Schema(minimum = "0", defaultValue = "0"))
          @RequestParam(value = "page", required = false, defaultValue = "0")
          Integer page,
      @Parameter(
              in = ParameterIn.QUERY,
              description = "The size of the page to be returned",
              schema = @Schema(minimum = "1", defaultValue = "20"))
          @RequestParam(value = "size", required = false, defaultValue = "20")
          Integer size,
      @Parameter(
              in = ParameterIn.QUERY,
              description =
                  "Sorting criteria in the format: property,(asc|desc). Default sort order is"
                      + " ascending. Multiple sort criteria are supported.",
              schema = @Schema())
          @RequestParam(value = "sort", required = false)
          List<String> sort);

  @Operation(
      summary = "",
      description = "Allow uploading a PDF document with metadata.",
      tags = {"Document Management"})
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "The document was uploaded successfully."),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Not Found",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Conflict",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "*/*", schema = @Schema(implementation = Object.class)))
      })
  @RequestMapping(
      value = "/document-management/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      method = RequestMethod.POST)
  ResponseEntity<Document> uploadDocument(
      @Parameter(
              in = ParameterIn.DEFAULT,
              description = "The user who uploaded the document.",
              required = true,
              schema = @Schema())
          @RequestParam(value = "user")
          String user,
      @Parameter(
              in = ParameterIn.DEFAULT,
              description = "The document name.",
              required = true,
              schema = @Schema())
          @RequestParam(value = "name")
          String name,
      @Parameter(
              in = ParameterIn.DEFAULT,
              description = "The document tags.",
              required = true,
              schema = @Schema())
          @RequestParam(value = "tags")
          List<String> tags,
      @Parameter(
              in = ParameterIn.DEFAULT,
              description = "The multipartfile file with binary data.",
              required = true,
              schema = @Schema())
          @RequestPart(value = "file")
          MultipartFile file);
}
