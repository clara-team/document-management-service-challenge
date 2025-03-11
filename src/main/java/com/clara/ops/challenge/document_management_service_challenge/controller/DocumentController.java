package com.clara.ops.challenge.document_management_service_challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/document")
@Tag(
    name = "Documents Management",
    description = "APIs for managing documents catalogue by Juan Pachon")
public class DocumentController {

  private final String uploadDir = "uploads/pdf";

  public DocumentController() {
    File directory = new File(uploadDir);
    if (!directory.exists()) {
      directory.mkdirs();
    }
  }

  @Operation(summary = "Create a new file", description = "Create a new file")
  @ApiResponse(responseCode = "201", description = "File uploaded successfully")
  @PostMapping(
      value = "/upload",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
    Map<String, Object> response = new HashMap<>();

    // Check if file is empty
    if (file.isEmpty()) {
      response.put("message", "Please select a file to upload");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Check if file is a PDF
    if (!file.getContentType().equals(MediaType.APPLICATION_PDF_VALUE)) {
      response.put("message", "Only PDF files are allowed");
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    try {
      // Generate a unique filename
      String originalFilename = file.getOriginalFilename();
      String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
      String newFilename = UUID.randomUUID().toString() + fileExtension;

      // Save the file
      Path filePath = Paths.get(uploadDir, newFilename);
      Files.write(filePath, file.getBytes());

      // Return success response
      response.put("message", "File uploaded successfully");
      response.put("filename", newFilename);
      response.put("originalFilename", originalFilename);
      response.put("size", file.getSize());
      response.put("path", filePath.toString());

      return new ResponseEntity<>(response, HttpStatus.OK);

    } catch (IOException e) {
      response.put("message", "Failed to upload file: " + e.getMessage());
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
