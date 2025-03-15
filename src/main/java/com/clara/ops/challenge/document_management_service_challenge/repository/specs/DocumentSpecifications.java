package com.clara.ops.challenge.document_management_service_challenge.repository.specs;

import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.User;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/** Specifications for filtering documents based on user, name, and tags. */
public class DocumentSpecifications {

  /**
   * Returns a specification that matches documents with the given user.
   *
   * @param user The user to match documents for.
   * @return A specification that matches documents with the given user.
   */
  public static Specification<Document> userEquals(User user) {
    return (root, query, criteriaBuilder) -> {
      if (user == null) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("user"), user);
    };
  }

  /**
   * Returns a specification that matches documents with the given name.
   *
   * @param name The name to match documents for.
   * @return A specification that matches documents with the given name.
   */
  public static Specification<Document> nameEquals(String name) {
    return (root, query, criteriaBuilder) -> {
      if (name == null || name.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      return criteriaBuilder.equal(root.get("name"), name);
    };
  }

  /**
   * Returns a specification that matches documents with any of the given tags.
   *
   * @param tags The tags to match documents for.
   * @return A specification that matches documents with any of the given tags.
   */
  public static Specification<Document> tagsContains(List<String> tags) {
    return (root, query, criteriaBuilder) -> {
      if (tags == null || tags.isEmpty()) {
        return criteriaBuilder.conjunction();
      }
      Predicate[] predicates =
          tags.stream()
              .map(tag -> criteriaBuilder.isMember(tag.trim(), root.get("tags")))
              .toArray(Predicate[]::new);
      return criteriaBuilder.or(predicates);
    };
  }
}
