package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.Tag;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.mapper.IDocumentEntityMapper;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DocumentManagementServiceImp implements IDocumentManagementService {

  @Autowired private IMinioService minioService;
  @Autowired private UserRepository userRepository;
  @Autowired private DocumentRepository documentRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private IDocumentEntityMapper documentEntityMapper;

  @Override
  public DocumentDTO uploadDocument(UploadDocumentDTO uploadDocumentDTO) {
    validateFile(uploadDocumentDTO.getFile());
    User user = validateUser(uploadDocumentDTO.getUser());
    FileResponseDTO fileResponseDTO =
        minioService.putObject(
            uploadDocumentDTO.getFile(), user.getName(), uploadDocumentDTO.getName());
    Document document = validateDocument(user, fileResponseDTO);
    List<Tag> listTags = validateListTags(document, uploadDocumentDTO.getTags());
    return DocumentDTO.builder()
        .id(document.getId().toString())
        .type(document.getFileType())
        .pathFile(document.getMinioPath())
        .size(document.getFileSize().intValue())
        .name(document.getName())
        .user(document.getUser().getName())
        .createdAt(document.getCreatedAt().toString())
        .tags(listTags.stream().map(Tag::getName).collect(Collectors.toList()))
        .build();
  }

  @Override
  public PaginatedDocumentSearchDTO searchDocuments(DocumentSearchDTO documentSearchDTO) {
    Sort sort =
        documentSearchDTO.getSort().equals("asc")
            ? Sort.by("createdAt").ascending()
            : Sort.by("createdAt").descending();
    Pageable pageable =
        PageRequest.of(documentSearchDTO.getPage(), documentSearchDTO.getSize(), sort);
    Page<Document> pageDocumentEntity =
        validateIsAllDocuments(documentSearchDTO)
            ? documentRepository.findAll(pageable)
            : documentRepository.findAllByUserNameAndNameAndTagName(
                documentSearchDTO.getUser(),
                documentSearchDTO.getName(),
                documentSearchDTO.getTags(),
                pageable);
    List<DocumentDTO> listDocumentDTO =
        pageDocumentEntity.getContent().stream()
            .map(document -> documentEntityMapper.mapToDocumentDTO(document))
            .toList();
    MetadataDTO metadataDTO =
        documentEntityMapper.mapToMetadataDTO(
            documentSearchDTO.getPage(),
            documentSearchDTO.getSize(),
            pageDocumentEntity.getNumberOfElements(),
            pageDocumentEntity.getTotalPages(),
            (int) pageDocumentEntity.getTotalElements());
    return PaginatedDocumentSearchDTO.builder()
        .metadata(metadataDTO)
        .documents(listDocumentDTO)
        .build();
  }

  @Override
  public DocumentDownloadUrlDTO downloadDocument(Integer documentId) {
    Document document = documentRepository.findById(documentId).orElse(null);
    if (Objects.isNull(document))
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found");
    return DocumentDownloadUrlDTO.builder()
        .url(minioService.getObjectUrl(document.getMinioPath()))
        .build();
  }

  private void validateFile(MultipartFile file) {
    if (!file.getContentType().equalsIgnoreCase("application/pdf"))
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "file type not allowed");
    if (file.getSize() > 500L * 1024 * 1024)
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "File size exceeds the 500mb limit");
  }

  private User validateUser(String name) {
    User user = userRepository.findOneByName(name).orElse(null);
    if (Objects.isNull(user)) user = new User();
    user.setName(name);
    return userRepository.save(user);
  }

  private Document validateDocument(User user, FileResponseDTO fileResponseDTO) {
    Document document =
        documentRepository.findOneByUserAndName(user, fileResponseDTO.getFilename()).orElse(null);
    if (Objects.isNull(document)) document = new Document();
    document.setUser(user);
    document.setName(fileResponseDTO.getFilename());
    document.setFileType(fileResponseDTO.getContentType());
    document.setFileSize(fileResponseDTO.getFileSize());
    document.setMinioPath(fileResponseDTO.getPathFile());
    document.setCreatedAt(LocalDateTime.now());
    return documentRepository.save(document);
  }

  private List<Tag> validateListTags(Document document, List<String> tags) {
    tagRepository.deleteAllByDocument(document);
    List<Tag> newListTags =
        tags.stream().map((tag) -> new Tag(null, tag, document)).collect(Collectors.toList());
    return tagRepository.saveAll(newListTags);
  }

  private boolean validateIsAllDocuments(DocumentSearchDTO documentSearchDTO) {
    return Objects.isNull(documentSearchDTO.getUser())
        && Objects.isNull(documentSearchDTO.getName())
        && Objects.isNull(documentSearchDTO.getTags());
  }
}
