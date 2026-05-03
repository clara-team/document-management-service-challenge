package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.model.request.DocumentSearchFilter;
import com.clara.ops.challenge.document_management_service_challenge.controller.model.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.Document;
import com.clara.ops.challenge.document_management_service_challenge.exception.DocumentNotFoundException;
import com.clara.ops.challenge.document_management_service_challenge.exception.UploadLimitException;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.Set;
import java.util.concurrent.Semaphore;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

  private static final Semaphore UPLOAD_SEMAPHORE = new Semaphore(10);

  private final FileUploadService fileUploadService;
  private final DocumentRepository documentRepository;

  public void upload(Document document, MultipartFile file) {
    if (!UPLOAD_SEMAPHORE.tryAcquire()) {
      throw new UploadLimitException("Upload capacity reached, please try again later");
    }
    try {
      String objectKey = buildObjectKey(document);
      fileUploadService.uploadFile(file, objectKey);
      document.setFilePath(objectKey);
      documentRepository.save(document);
    } finally {
      UPLOAD_SEMAPHORE.release();
    }
  }

  public DocumentResponse getDownloadUrlById(Long id) {
    var document =
        documentRepository
            .findById(id)
            .orElseThrow(
                () -> new DocumentNotFoundException(String.format("Document not found: %d", id)));
    String url = fileUploadService.getDownloadLink(document.getFilePath());
    return new DocumentResponse(document.getId(), document.getDocumentName(), url);
  }

  public Page<Document> filter(DocumentSearchFilter filter, PageRequest pageRequest) {
    Specification<Document> specification =
        Specification.where(hasName(filter.name()))
            .and(hasUser(filter.user()))
            .and(hasTags(filter.tags()));
    return documentRepository.findAll(
        specification, pageRequest.withSort(Sort.by(Sort.Direction.DESC, "createdAt")));
  }

  private static String buildObjectKey(Document document) {
    return document.getUserId().trim().toLowerCase()
        + "/"
        + document.getDocumentName().trim().toLowerCase()
        + ".pdf";
  }

  private static Specification<Document> hasName(String name) {
    return (root, query, criteriaBuilder) ->
        StringUtils.hasText(name)
            ? criteriaBuilder.equal(
                criteriaBuilder.lower(root.get("documentName")), name.toLowerCase())
            : null;
  }

  private static Specification<Document> hasUser(String user) {
    return (root, query, criteriaBuilder) ->
        StringUtils.hasText(user)
            ? criteriaBuilder.equal(criteriaBuilder.lower(root.get("userId")), user.toLowerCase())
            : null;
  }

  private static Specification<Document> hasTags(Set<String> tags) {
    return (root, query, cb) -> {
      if (tags == null || tags.isEmpty()) return null;

      Subquery<Long> subquery = query.subquery(Long.class);
      Root<Document> subRoot = subquery.correlate(root);
      Join<Document, String> tagJoin = subRoot.join("tags");

      subquery.select(cb.countDistinct(tagJoin)).where(tagJoin.in(tags));

      return cb.equal(subquery, (long) tags.size());
    };
  }
}
