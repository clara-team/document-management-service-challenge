package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {}
