package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.model.entity.Document;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

  Page<Document> findAllByOrderByCreatedAtDesc(Pageable pageable);

  Page<Document> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

  Page<Document> findByDocumentNameContainingIgnoreCaseOrderByCreatedAtDesc(
      String documentName, Pageable pageable);

  @Query(
      "SELECT DISTINCT d FROM Document d JOIN d.tags t WHERE t.name IN :tagNames GROUP BY d.id"
          + " HAVING COUNT(DISTINCT t.name) = :tagCount ORDER BY d.createdAt DESC")
  Page<Document> findByTagNamesExactMatch(
      @Param("tagNames") List<String> tagNames,
      @Param("tagCount") Long tagCount,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT d FROM Document d JOIN d.tags t WHERE d.userId = :userId AND t.name IN"
          + " :tagNames GROUP BY d.id HAVING COUNT(DISTINCT t.name) = :tagCount ORDER BY"
          + " d.createdAt DESC")
  Page<Document> findByUserIdAndTagNamesExactMatch(
      @Param("userId") String userId,
      @Param("tagNames") List<String> tagNames,
      @Param("tagCount") Long tagCount,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT d FROM Document d JOIN d.tags t WHERE d.documentName LIKE %:documentName%"
          + " AND t.name IN :tagNames GROUP BY d.id HAVING COUNT(DISTINCT t.name) = :tagCount ORDER"
          + " BY d.createdAt DESC")
  Page<Document> findByDocumentNameAndTagNamesExactMatch(
      @Param("documentName") String documentName,
      @Param("tagNames") List<String> tagNames,
      @Param("tagCount") Long tagCount,
      Pageable pageable);

  @Query(
      "SELECT DISTINCT d FROM Document d JOIN d.tags t WHERE d.userId = :userId AND d.documentName"
          + " LIKE %:documentName% AND t.name IN :tagNames GROUP BY d.id HAVING COUNT(DISTINCT"
          + " t.name) = :tagCount ORDER BY d.createdAt DESC")
  Page<Document> findByUserIdAndDocumentNameAndTagNamesExactMatch(
      @Param("userId") String userId,
      @Param("documentName") String documentName,
      @Param("tagNames") List<String> tagNames,
      @Param("tagCount") Long tagCount,
      Pageable pageable);

  Page<Document> findByUserIdAndDocumentNameContainingIgnoreCaseOrderByCreatedAtDesc(
      String userId, String documentName, Pageable pageable);
}
