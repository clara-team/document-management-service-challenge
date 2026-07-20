package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.DocumentTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;

public interface DocumentTagRepository extends JpaRepository<DocumentTagEntity, Long> {


    List<DocumentTagEntity> findByDocumentId(UUID documentId);

    List<DocumentTagEntity> findByTag(String tag);

}