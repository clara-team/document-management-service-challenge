package com.clara.ops.challenge.document_management_service_challenge.domain.repository;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentEntity;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class DocumentSpecifications {

    private DocumentSpecifications() {}

    public static Specification<DocumentEntity> userEquals(String userName) {
        return (root, query, cb) ->
                cb.equal(cb.lower(root.get("userName")), userName.toLowerCase());
    }

    public static Specification<DocumentEntity> nameLike(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    public static Specification<DocumentEntity> hasAllTags(List<String> tags) {
        return (root, query, cb) -> {
            var predicates = tags.stream()
                    .map(tag -> cb.isMember(tag, root.get("tags")))
                    .toArray(jakarta.persistence.criteria.Predicate[]::new);
            return cb.and(predicates);
        };
    }

}
