package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Integer> {
  @Query(
      "SELECT DISTINCT d FROM Document d JOIN d.user u JOIN d.tags t WHERE u.name LIKE %:userName%"
          + " AND d.name LIKE %:documentName% AND t.name IN :tagNames GROUP BY"
          + " d.id HAVING COUNT(DISTINCT t.name) = 1")
  Page<Document> findAllByUserNameAndNameAndTagName(
      String userName, String documentName, List<String> tagNames, Pageable pageable);

  Optional<Document> findOneByUserAndName(User user, String name);
}
