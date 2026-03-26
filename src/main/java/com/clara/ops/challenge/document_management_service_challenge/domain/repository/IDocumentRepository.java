package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface IDocumentRepository extends JpaRepository<Document, Integer> {

    String QUERY_SEARCH_BY_FILTER = "WHERE (:user IS NULL OR d.username ILIKE :user) " +
            "AND (:name IS NULL OR d.filename ILIKE :name) " +
            "AND (:tags IS NULL OR dt.tagName IN :tags)";

    @Query(value = "SELECT distinct d FROM Document d " +
                        "LEFT JOIN d.documentTags dt " + QUERY_SEARCH_BY_FILTER,
    countQuery = "SELECT count(distinct d) FROM Document d " +
                        "LEFT JOIN d.documentTags dt " + QUERY_SEARCH_BY_FILTER)
    Page<Document> searchByFilters(
            @Param("user") String user,
            @Param("name") String name,
            @Param("tags") Set<String> tags,
            Pageable pageable
    );

}
