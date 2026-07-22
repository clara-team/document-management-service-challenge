package com.clara.ops.challenge.document_management_service_challenge.application.service;

import com.clara.ops.challenge.document_management_service_challenge.application.port.in.FindDocumentMetadataUserCase;
import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.exception.DocumentNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FindDocumentMetadataService implements FindDocumentMetadataUserCase {

  private final DocumentRepositoryPort repository;

  @Override
  public Document findMetadata(UUID documentId) {
    return repository
        .findById(documentId)
        .orElseThrow(() -> new DocumentNotFoundException(documentId.toString()));
  }
}
