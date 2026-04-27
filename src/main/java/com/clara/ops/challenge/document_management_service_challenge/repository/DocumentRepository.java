package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Page<DocumentEntity> findByUserName(String userName, Pageable pageable);

    Page<DocumentEntity> findByDocumentName(String documentName, Pageable pageable);

    Page<DocumentEntity> findByIdIn(List<UUID> ids, Pageable pageable);
}