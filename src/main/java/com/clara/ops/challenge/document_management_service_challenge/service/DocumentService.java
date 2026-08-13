package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entities.Document;
import com.clara.ops.challenge.document_management_service_challenge.repositories.DocumentRepository;
import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

  private final DocumentRepository documentRepository;
  private final MinioService minioService;

  public DocumentService(DocumentRepository documentRepository, MinioService minioService) {
    this.documentRepository = documentRepository;
    this.minioService = minioService;
  }

  public Document uploadDocument(
      String user, String documentName, List<String> tags, MultipartFile file) {
    try (InputStream inputStream = file.getInputStream()) {
      String objectName =
          minioService.uploadFile(
              user, documentName, inputStream, file.getSize(), file.getContentType());

      Document document = new Document();
      document.setUsername(user);
      document.setDocumentName(documentName);
      document.setTags(tags);
      document.setMinioPath(objectName);
      document.setFileSize(file.getSize());
      document.setFileType(file.getContentType());
      document.setCreatedAt(LocalDateTime.now());

      return documentRepository.save(document);
    } catch (IOException e) {
      throw new RuntimeException("Error processing file!: ", e);
    }
  }

  public Page<Document> searchDocuments(
      Optional<String> user,
      Optional<String> documentName,
      Optional<List<String>> tags,
      Pageable pageable) {
    Specification<Document> spec =
        (root, query, criteriaBuilder) -> {
          List<Predicate> predicates = new ArrayList<>();
          user.ifPresent(u -> predicates.add(criteriaBuilder.equal(root.get("username"), u)));
          documentName.ifPresent(
              name ->
                  predicates.add(criteriaBuilder.like(root.get("documentName"), "%" + name + "%")));
          tags.ifPresent(
              tagList -> {
                for (String tag : tagList) {
                  predicates.add(criteriaBuilder.isMember(tag, root.get("tags")));
                }
              });
          return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

    return documentRepository.findAll(spec, pageable);
  }

  public String getDownloadUrl(Long documentId) {
    Document document =
        documentRepository
            .findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found!"));
    return minioService.getPresignedUrl(document.getMinioPath());
  }
}
