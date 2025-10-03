package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentRepositoryPort;
import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DocumentRepositoryAdapter implements DocumentRepositoryPort {

  private final DocumentRepository documentRepository;

  @Override
  public void save(Document document) {

    DocumentEntity entity = new DocumentEntity();
    entity.setId(document.getId());
    entity.setUserId(document.getUserId());
    entity.setDocumentName(document.getDocumentName());
    entity.setFileSize(document.getFileSize());
    entity.setTags(document.getTags());
    entity.setChecksum(document.getChecksum());
    entity.setStatus(document.getStatus());
    entity.setMinioPath(document.getMinioPath());
    entity.setCreatedAt(Instant.now());

    documentRepository.save(entity);
  }
}
