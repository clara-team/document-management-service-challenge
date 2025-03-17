package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.config.MinioConfig;
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
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class DocumentManagementServiceImp implements IDocumentManagementService {

  @Autowired private UserRepository userRepository;
  @Autowired private DocumentRepository documentRepository;
  @Autowired private TagRepository tagRepository;
  @Autowired private IDocumentEntityMapper documentEntityMapper;
  @Autowired private IMinioService minioService;
  @Autowired private MinioConfig minioConfig;

  @Autowired
  @Qualifier("FilePartitionUploadStrategy") private IUploadStrategy filePartitionUploadStrategy;

  @Autowired
  @Qualifier("DiskUploadStrategy") private IUploadStrategy diskUploadStrategy;

  @Autowired
  @Qualifier("SemaphoreUploadStrategy") private IUploadStrategy semaphoreUploadStrategy;

  @Override
  public DocumentDTO validateStrategyAndUploadFile(UploadDocumentDTO uploadDocumentDTO) {
    validateConstrainsFile(uploadDocumentDTO.getFile());
    UploadType uploadType = uploadDocumentDTO.getTypeUpload();
    FileInputDTO fileInputDTO = getFileInputDTO(uploadDocumentDTO);
    if (UploadType.FILE_PARTITION.equals(uploadType))
      filePartitionUploadStrategy.processUpload(fileInputDTO, uploadDocumentDTO.getFile());
    else if (UploadType.DISK_UPLOAD.equals(uploadType))
      diskUploadStrategy.processUpload(fileInputDTO, uploadDocumentDTO.getFile());
    else if (UploadType.SEMAPHORE.equals(uploadType))
      semaphoreUploadStrategy.processUpload(fileInputDTO, uploadDocumentDTO.getFile());
    User user = validateAndCreateUser(uploadDocumentDTO.getUser());
    Document document = validateAndCreateDocument(user, fileInputDTO);
    List<Tag> listTags = deleteAndCreateNewListTags(document, uploadDocumentDTO.getTags());
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

  @NotNull private static FileInputDTO getFileInputDTO(UploadDocumentDTO uploadDocumentDTO) {
    String fileName = uploadDocumentDTO.getFile().getOriginalFilename();
    String fileType = uploadDocumentDTO.getFile().getContentType();
    String objectName = uploadDocumentDTO.getName() + fileName.substring(fileName.lastIndexOf("."));
    String pathFile = uploadDocumentDTO.getUser() + "/" + objectName;
    FileInputDTO fileInputDTO = new FileInputDTO();
    fileInputDTO.setNameDocument(fileName);
    fileInputDTO.setFileType(fileType);
    fileInputDTO.setPathFile(pathFile);
    fileInputDTO.setFileSize(uploadDocumentDTO.getFile().getSize());
    return fileInputDTO;
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
        .url(minioService.getObjectUrl(minioConfig.getBucketName(), document.getMinioPath()))
        .build();
  }

  private void validateConstrainsFile(MultipartFile file) {
    if (file.isEmpty() || !Objects.equals(file.getContentType(), MediaType.APPLICATION_PDF_VALUE))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed");
    if (file.getSize() > 500 * 1024 * 1024)
      throw new ResponseStatusException(
          HttpStatus.PAYLOAD_TOO_LARGE,
          "File size exceeds the maximum limit allowed size of 500Mb");
  }

  private User validateAndCreateUser(String name) {
    User user = userRepository.findOneByName(name).orElse(null);
    if (Objects.isNull(user)) user = new User();
    user.setName(name);
    return userRepository.save(user);
  }

  private Document validateAndCreateDocument(User user, FileInputDTO fileInputDTO) {
    Document document =
        documentRepository.findOneByUserAndName(user, fileInputDTO.getNameDocument()).orElse(null);
    if (Objects.isNull(document)) document = new Document();
    document.setUser(user);
    document.setName(fileInputDTO.getNameDocument());
    document.setFileType(fileInputDTO.getFileType());
    document.setFileSize(fileInputDTO.getFileSize());
    document.setMinioPath(fileInputDTO.getPathFile());
    document.setCreatedAt(LocalDateTime.now());
    return documentRepository.save(document);
  }

  private List<Tag> deleteAndCreateNewListTags(Document document, List<String> tags) {
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
