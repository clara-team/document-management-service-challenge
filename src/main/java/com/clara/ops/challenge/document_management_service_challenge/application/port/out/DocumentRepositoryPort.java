package com.clara.ops.challenge.document_management_service_challenge.application.port.out;

import com.clara.ops.challenge.document_management_service_challenge.domain.model.Document;

public interface DocumentRepositoryPort {
  void save(Document document);
}
