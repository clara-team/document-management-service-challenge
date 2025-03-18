package com.clara.ops.challenge.document_management_service_challenge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.entity.Tag;
import com.clara.ops.challenge.document_management_service_challenge.entity.User;
import com.clara.ops.challenge.document_management_service_challenge.repository.DocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.TagRepository;
import com.clara.ops.challenge.document_management_service_challenge.repository.UserRepository;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.DocumentDTO;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.FileInputDTO;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.UploadDocumentDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class DocumentManagementServiceTest {

  @InjectMocks private DocumentManagementServiceImp documentManagementServiceImp;
  @Mock private UserRepository userRepository;
  @Mock private DocumentRepository documentRepository;
  @Mock private TagRepository tagRepository;

  @Mock
  @Qualifier("FilePartitionUploadStrategy") private IUploadStrategy filePartitionUploadStrategy;

  @Mock
  @Qualifier("DiskUploadStrategy") private IUploadStrategy diskUploadStrategy;

  @Mock
  @Qualifier("SemaphoreUploadStrategy") private IUploadStrategy semaphoreUploadStrategy;

  private LocalDateTime localDateTime;
  private MockMultipartFile largeFileWith500MbSize;

  @BeforeEach
  void setUp() {
    localDateTime = LocalDateTime.now();
    largeFileWith500MbSize =
        new MockMultipartFile(
            "500Mb", "500Mb.pdf", MediaType.APPLICATION_PDF_VALUE, "Any content".getBytes()) {
          @Override
          public long getSize() {
            return 500 * 1024 * 1024;
          }
        };
  }

  @Test
  void validateStrategyAndUploadFile_Should_CreatedFile_WithStrategy_FilePartition() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.FILE_PARTITION);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    FileInputDTO fileInputDTO = new FileInputDTO();
    fileInputDTO.setNameDocument("Document.pdf");
    fileInputDTO.setFileType(MediaType.APPLICATION_JSON_VALUE);
    fileInputDTO.setPathFile("User/Document.pdf");
    fileInputDTO.setFileSize(largeFileWith500MbSize.getSize());

    User user = new User();
    user.setId(1);
    user.setName("User");

    Document document = new Document();
    document.setId(1);
    document.setUser(user);
    document.setName(fileInputDTO.getNameDocument());
    document.setMinioPath(fileInputDTO.getPathFile());
    document.setFileSize(fileInputDTO.getFileSize());
    document.setFileType(fileInputDTO.getFileType());
    document.setCreatedAt(localDateTime);

    List<Tag> tags =
        uploadDocumentDTO.getTags().stream().map((tag) -> new Tag(null, tag, document)).toList();

    document.setTags(tags);

    when(userRepository.save(any(User.class))).thenReturn(user);
    when(documentRepository.save(any(Document.class))).thenReturn(document);
    when(tagRepository.saveAll(anyList())).thenReturn(tags);

    DocumentDTO documentDTO =
        documentManagementServiceImp.validateStrategyAndUploadFile(uploadDocumentDTO);

    assertEquals(documentDTO.getId(), document.getId().toString());
    assertEquals(documentDTO.getUser(), document.getUser().getName());
    assertEquals(documentDTO.getName(), document.getName());
    assertEquals(
        documentDTO.getTags(), tags.stream().map(Tag::getName).collect(Collectors.toList()));
    assertEquals(documentDTO.getSize(), document.getFileSize().intValue());
    assertEquals(documentDTO.getType(), document.getFileType());
    assertEquals(documentDTO.getCreatedAt(), document.getCreatedAt().toString());
    verify(filePartitionUploadStrategy, times(1))
        .processUpload(any(FileInputDTO.class), any(MockMultipartFile.class));
    verify(tagRepository, times(1)).deleteAllByDocument(any(Document.class));
  }

  @Test
  void validateStrategyAndUploadFile_Should_CreatedFile_WithStrategy_DiskUpload() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.DISK_UPLOAD);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    FileInputDTO fileInputDTO = new FileInputDTO();
    fileInputDTO.setNameDocument("Document.pdf");
    fileInputDTO.setFileType(MediaType.APPLICATION_JSON_VALUE);
    fileInputDTO.setPathFile("User/Document.pdf");
    fileInputDTO.setFileSize(largeFileWith500MbSize.getSize());

    User user = new User();
    user.setId(1);
    user.setName("User");

    Document document = new Document();
    document.setId(1);
    document.setUser(user);
    document.setName(fileInputDTO.getNameDocument());
    document.setMinioPath(fileInputDTO.getPathFile());
    document.setFileSize(fileInputDTO.getFileSize());
    document.setFileType(fileInputDTO.getFileType());
    document.setCreatedAt(localDateTime);

    List<Tag> tags =
        uploadDocumentDTO.getTags().stream().map((tag) -> new Tag(null, tag, document)).toList();

    document.setTags(tags);

    when(userRepository.save(any(User.class))).thenReturn(user);
    when(documentRepository.save(any(Document.class))).thenReturn(document);
    when(tagRepository.saveAll(anyList())).thenReturn(tags);

    DocumentDTO documentDTO =
        documentManagementServiceImp.validateStrategyAndUploadFile(uploadDocumentDTO);

    assertEquals(documentDTO.getId(), document.getId().toString());
    assertEquals(documentDTO.getUser(), document.getUser().getName());
    assertEquals(documentDTO.getName(), document.getName());
    assertEquals(
        documentDTO.getTags(), tags.stream().map(Tag::getName).collect(Collectors.toList()));
    assertEquals(documentDTO.getSize(), document.getFileSize().intValue());
    assertEquals(documentDTO.getType(), document.getFileType());
    assertEquals(documentDTO.getCreatedAt(), document.getCreatedAt().toString());
    verify(diskUploadStrategy, times(1))
        .processUpload(any(FileInputDTO.class), any(MockMultipartFile.class));
    verify(tagRepository, times(1)).deleteAllByDocument(any(Document.class));
  }

  @Test
  void validateStrategyAndUploadFile_Should_CreatedFile_WithStrategy_Semaphore() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.SEMAPHORE);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    FileInputDTO fileInputDTO = new FileInputDTO();
    fileInputDTO.setNameDocument("Document.pdf");
    fileInputDTO.setFileType(MediaType.APPLICATION_JSON_VALUE);
    fileInputDTO.setPathFile("User/Document.pdf");
    fileInputDTO.setFileSize(largeFileWith500MbSize.getSize());

    User user = new User();
    user.setId(1);
    user.setName("User");

    Document document = new Document();
    document.setId(1);
    document.setUser(user);
    document.setName(fileInputDTO.getNameDocument());
    document.setMinioPath(fileInputDTO.getPathFile());
    document.setFileSize(fileInputDTO.getFileSize());
    document.setFileType(fileInputDTO.getFileType());
    document.setCreatedAt(localDateTime);

    List<Tag> tags =
        uploadDocumentDTO.getTags().stream().map((tag) -> new Tag(null, tag, document)).toList();

    document.setTags(tags);

    when(userRepository.save(any(User.class))).thenReturn(user);
    when(documentRepository.save(any(Document.class))).thenReturn(document);
    when(tagRepository.saveAll(anyList())).thenReturn(tags);

    DocumentDTO documentDTO =
        documentManagementServiceImp.validateStrategyAndUploadFile(uploadDocumentDTO);

    assertEquals(documentDTO.getId(), document.getId().toString());
    assertEquals(documentDTO.getUser(), document.getUser().getName());
    assertEquals(documentDTO.getName(), document.getName());
    assertEquals(
        documentDTO.getTags(), tags.stream().map(Tag::getName).collect(Collectors.toList()));
    assertEquals(documentDTO.getSize(), document.getFileSize().intValue());
    assertEquals(documentDTO.getType(), document.getFileType());
    assertEquals(documentDTO.getCreatedAt(), document.getCreatedAt().toString());
    verify(semaphoreUploadStrategy, times(1))
        .processUpload(any(FileInputDTO.class), any(MockMultipartFile.class));
    verify(tagRepository, times(1)).deleteAllByDocument(any(Document.class));
  }
}
