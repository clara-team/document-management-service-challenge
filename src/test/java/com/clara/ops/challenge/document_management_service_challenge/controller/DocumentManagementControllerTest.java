package com.clara.ops.challenge.document_management_service_challenge.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.clara.ops.challenge.document_management_service_challenge.controller.dto.request.DocumentSearchFilters;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Document;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.DocumentDownloadUrl;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.Metadata;
import com.clara.ops.challenge.document_management_service_challenge.controller.dto.response.PaginatedDocumentSearch;
import com.clara.ops.challenge.document_management_service_challenge.mapper.IDocumentManagementMapper;
import com.clara.ops.challenge.document_management_service_challenge.service.IDocumentManagementService;
import com.clara.ops.challenge.document_management_service_challenge.service.IUploadStrategy;
import com.clara.ops.challenge.document_management_service_challenge.service.UploadType;
import com.clara.ops.challenge.document_management_service_challenge.service.dto.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
public class DocumentManagementControllerTest {

  @InjectMocks private DocumentManagementControllerImp documentManagementControllerImp;
  @Mock private IDocumentManagementService documentManagementService;

  @Mock
  @Qualifier("FilePartitionUploadStrategy") private IUploadStrategy filePartitionUploadStrategy;

  @Mock
  @Qualifier("DiskUploadStrategy") private IUploadStrategy diskUploadStrategy;

  @Mock
  @Qualifier("SemaphoreUploadStrategy") private IUploadStrategy semaphoreUploadStrategy;

  @Mock private IDocumentManagementMapper documentManagementMapper;

  private String localDateTime;
  private MockMultipartFile largeFileWith500MbSize;
  private MockMultipartFile largeFileWith510MbSize;
  private MockMultipartFile typeTextFile;

  @BeforeEach
  void setUp() {
    localDateTime = LocalDateTime.now().toString();
    largeFileWith500MbSize =
        new MockMultipartFile(
            "500Mb", "500Mb.pdf", MediaType.APPLICATION_PDF_VALUE, "Any content".getBytes()) {
          @Override
          public long getSize() {
            return 500 * 1024 * 1024;
          }
        };
    largeFileWith510MbSize =
        new MockMultipartFile(
            "510Mb", "510Mb.pdf", MediaType.APPLICATION_PDF_VALUE, "Any content".getBytes()) {
          @Override
          public long getSize() {
            return 510 * 1024 * 1024;
          }
        };
    typeTextFile =
        new MockMultipartFile(
            "file", "file.txt", MediaType.TEXT_PLAIN_VALUE, "Any content".getBytes());
  }

  @Test
  void uploadDocument_Should_Return200_WithTypeUpload_FilePartition() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.FILE_PARTITION);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    DocumentDTO documentDTO = new DocumentDTO();
    documentDTO.setId("1");
    documentDTO.setName("Document");
    documentDTO.setUser("User");
    documentDTO.setTags(List.of("Tag"));
    documentDTO.setType(MediaType.APPLICATION_JSON_VALUE);
    documentDTO.setPathFile("User/Document.pdf");
    documentDTO.setCreatedAt(localDateTime);

    Document document = new Document();
    document.setId("1");
    document.setName("Document");
    document.setUser("User");
    document.setTags(List.of("Tag"));
    document.setType(MediaType.APPLICATION_JSON_VALUE);
    document.setSize(500000000);
    document.setCreatedAt(localDateTime);

    FileInputDTO fileInputDTO = new FileInputDTO();
    fileInputDTO.setNameDocument("Document");
    fileInputDTO.setPathFile("User/Document.pdf");
    fileInputDTO.setFileSize(500000000L);
    fileInputDTO.setFileType(MediaType.APPLICATION_JSON_VALUE);

    when(documentManagementMapper.mapToUploadDocumentDTO(
            anyString(), anyString(), anyList(), anyString(), any()))
        .thenReturn(uploadDocumentDTO);
    when(documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO))
        .thenReturn(documentDTO);
    when(documentManagementMapper.mapToDocument(documentDTO)).thenReturn(document);

    ResponseEntity<Document> response =
        documentManagementControllerImp.uploadDocument(
            "User",
            "Document",
            List.of("Tag"),
            UploadType.FILE_PARTITION.toString(),
            largeFileWith500MbSize);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(document, response.getBody());
    verify(documentManagementService, times(1)).validateStrategyAndUploadFile(uploadDocumentDTO);
  }

  @Test
  void uploadDocument_Should_Return200_WithTypeUpload_DiskUpload() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.DISK_UPLOAD);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    DocumentDTO documentDTO = new DocumentDTO();
    documentDTO.setId("1");
    documentDTO.setName("Document");
    documentDTO.setUser("User");
    documentDTO.setTags(List.of("Tag"));
    documentDTO.setType(MediaType.APPLICATION_JSON_VALUE);
    documentDTO.setPathFile("User/Document.pdf");
    documentDTO.setCreatedAt(localDateTime);

    Document document = new Document();
    document.setId("1");
    document.setName("Document");
    document.setUser("User");
    document.setTags(List.of("Tag"));
    document.setType(MediaType.APPLICATION_JSON_VALUE);
    document.setSize(500000000);
    document.setCreatedAt(localDateTime);

    when(documentManagementMapper.mapToUploadDocumentDTO(
            anyString(), anyString(), anyList(), anyString(), any()))
        .thenReturn(uploadDocumentDTO);
    when(documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO))
        .thenReturn(documentDTO);
    when(documentManagementMapper.mapToDocument(documentDTO)).thenReturn(document);

    ResponseEntity<Document> response =
        documentManagementControllerImp.uploadDocument(
            "User",
            "Document",
            List.of("Tag"),
            UploadType.DISK_UPLOAD.toString(),
            largeFileWith500MbSize);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(document, response.getBody());
    verify(documentManagementService, times(1)).validateStrategyAndUploadFile(uploadDocumentDTO);
  }

  @Test
  void uploadDocument_Should_Return200_WithTypeUpload_Semaphore() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.SEMAPHORE);
    uploadDocumentDTO.setFile(largeFileWith500MbSize);

    DocumentDTO documentDTO = new DocumentDTO();
    documentDTO.setId("1");
    documentDTO.setName("Document");
    documentDTO.setUser("User");
    documentDTO.setTags(List.of("Tag"));
    documentDTO.setType(MediaType.APPLICATION_JSON_VALUE);
    documentDTO.setPathFile("User/Document.pdf");
    documentDTO.setCreatedAt(localDateTime);

    Document document = new Document();
    document.setId("1");
    document.setName("Document");
    document.setUser("User");
    document.setTags(List.of("Tag"));
    document.setType(MediaType.APPLICATION_JSON_VALUE);
    document.setSize(500000000);
    document.setCreatedAt(localDateTime);

    when(documentManagementMapper.mapToUploadDocumentDTO(
            anyString(), anyString(), anyList(), anyString(), any()))
        .thenReturn(uploadDocumentDTO);
    when(documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO))
        .thenReturn(documentDTO);
    when(documentManagementMapper.mapToDocument(documentDTO)).thenReturn(document);

    ResponseEntity<Document> response =
        documentManagementControllerImp.uploadDocument(
            "User",
            "Document",
            List.of("Tag"),
            UploadType.SEMAPHORE.toString(),
            largeFileWith500MbSize);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(document, response.getBody());
    verify(documentManagementService, times(1)).validateStrategyAndUploadFile(uploadDocumentDTO);
  }

  @Test
  void uploadDocument_Should_Return400_WithTypeFile_Invalid() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.FILE_PARTITION);
    uploadDocumentDTO.setFile(typeTextFile);

    when(documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO))
        .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed"));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO));

    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    assertEquals("File type not allowed", exception.getReason());
    verify(documentManagementService, times(1)).validateStrategyAndUploadFile(uploadDocumentDTO);
  }

  @Test
  void uploadDocument_Should_Return413_WithLargeTypeFile_SizeExceedsAllowedLimit() {
    UploadDocumentDTO uploadDocumentDTO = new UploadDocumentDTO();
    uploadDocumentDTO.setUser("User");
    uploadDocumentDTO.setName("Document");
    uploadDocumentDTO.setTags(List.of("Tag"));
    uploadDocumentDTO.setTypeUpload(UploadType.FILE_PARTITION);
    uploadDocumentDTO.setFile(largeFileWith510MbSize);

    when(documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO))
        .thenThrow(
            new ResponseStatusException(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "File size exceeds the maximum limit allowed size of 500Mb"));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> documentManagementService.validateStrategyAndUploadFile(uploadDocumentDTO));

    assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, exception.getStatusCode());
    assertEquals(
        "File size exceeds the maximum limit allowed size of 500Mb", exception.getReason());
    verify(documentManagementService, times(1)).validateStrategyAndUploadFile(uploadDocumentDTO);
  }

  @Test
  void downloadDocument_Should_Return200_WithDocumentExists() {
    Integer documentId = anyInt();
    String documentUrl =
        "http://host.docker.internal:9000/document-bucket/User/Documento.pdf?any-text";
    DocumentDownloadUrl documentDownloadUrl = new DocumentDownloadUrl();
    documentDownloadUrl.setUrl(documentUrl);
    DocumentDownloadUrlDTO documentDownloadUrlDTO = new DocumentDownloadUrlDTO();
    documentDownloadUrlDTO.setUrl(documentUrl);

    when(documentManagementService.downloadDocument(documentId)).thenReturn(documentDownloadUrlDTO);
    when(documentManagementMapper.mapDocumentDownloadUrl(documentDownloadUrlDTO))
        .thenReturn(documentDownloadUrl);

    ResponseEntity<DocumentDownloadUrl> response =
        documentManagementControllerImp.downloadDocument(documentId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(documentDownloadUrl, response.getBody());
    verify(documentManagementService, times(1)).downloadDocument(documentId);
  }

  @Test
  void downloadDocument_Should_Return400_WithDocumentNotExists() {
    Integer documentId = anyInt();

    when(documentManagementService.downloadDocument(documentId))
        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

    ResponseStatusException exception =
        assertThrows(
            ResponseStatusException.class,
            () -> documentManagementService.downloadDocument(documentId));

    assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    assertEquals("Document not found", exception.getReason());
    verify(documentManagementService, times(1)).downloadDocument(documentId);
  }

  @Test
  void searchDocuments_Should_Return200_WithoutFilterAndReturnAllDocument() {
    Document documentOne = new Document();
    documentOne.setId("1");
    documentOne.setUser("UserOne");
    documentOne.setName("DocumentOne");
    documentOne.setTags(List.of("Tag"));
    documentOne.setSize(50 * 1024 * 10240);
    documentOne.setType(MediaType.APPLICATION_JSON_VALUE);
    documentOne.setCreatedAt(localDateTime);

    Document documentTwo = new Document();
    documentTwo.setId("2");
    documentTwo.setUser("UserTwo");
    documentTwo.setName("DocumentTwo");
    documentTwo.setTags(List.of("Tag"));
    documentTwo.setSize(30 * 1024 * 10240);
    documentTwo.setType(MediaType.APPLICATION_JSON_VALUE);
    documentTwo.setCreatedAt(localDateTime);

    Document documentThree = new Document();
    documentThree.setId("3");
    documentThree.setUser("UserThree");
    documentThree.setName("DocumentThree");
    documentThree.setTags(List.of("Tag"));
    documentThree.setSize(40 * 1024 * 10240);
    documentThree.setType(MediaType.APPLICATION_JSON_VALUE);
    documentThree.setCreatedAt(localDateTime);

    Metadata metadata = new Metadata();
    metadata.setCurrentPage(0);
    metadata.setItemsPerPage(3);
    metadata.setCurrentItems(3);
    metadata.setTotalPages(1);
    metadata.setTotalItems(3);

    PaginatedDocumentSearch paginatedDocumentSearch = new PaginatedDocumentSearch();
    paginatedDocumentSearch.setMetadata(metadata);
    paginatedDocumentSearch.setDocuments(List.of(documentOne, documentTwo, documentThree));

    DocumentSearchFilters documentSearchFilters = new DocumentSearchFilters();
    documentSearchFilters.setUser(null);
    documentSearchFilters.setName(null);
    documentSearchFilters.setTags(null);
    Integer page = 0;
    Integer size = 3;
    String sort = "desc";

    DocumentSearchDTO documentSearchDTO = new DocumentSearchDTO();
    documentSearchDTO.setUser(documentSearchFilters.getUser());
    documentSearchDTO.setName(documentSearchFilters.getName());
    documentSearchDTO.setTags(documentSearchFilters.getTags());
    documentSearchDTO.setPage(page);
    documentSearchDTO.setSize(size);
    documentSearchDTO.setSort(sort);

    DocumentDTO documentDTOOne = new DocumentDTO();
    documentDTOOne.setId(documentOne.getId());
    documentDTOOne.setUser(documentOne.getUser());
    documentDTOOne.setName(documentOne.getName());
    documentDTOOne.setTags(documentOne.getTags());
    documentDTOOne.setSize(documentOne.getSize());
    documentDTOOne.setType(documentDTOOne.getType());
    documentDTOOne.setCreatedAt(documentOne.getCreatedAt());

    DocumentDTO documentDTOTwo = new DocumentDTO();
    documentDTOTwo.setId(documentTwo.getId());
    documentDTOTwo.setUser(documentTwo.getUser());
    documentDTOTwo.setName(documentTwo.getName());
    documentDTOTwo.setTags(documentTwo.getTags());
    documentDTOTwo.setSize(documentOne.getSize());
    documentDTOTwo.setType(documentTwo.getType());
    documentDTOTwo.setCreatedAt(documentTwo.getCreatedAt());

    DocumentDTO documentDTOThree = new DocumentDTO();
    documentDTOThree.setId(documentThree.getId());
    documentDTOThree.setUser(documentThree.getUser());
    documentDTOThree.setName(documentThree.getName());
    documentDTOThree.setTags(documentThree.getTags());
    documentDTOThree.setSize(documentThree.getSize());
    documentDTOThree.setType(documentThree.getType());
    documentDTOThree.setCreatedAt(documentThree.getCreatedAt());

    MetadataDTO metadataDTO = new MetadataDTO();
    metadataDTO.setCurrentPage(metadata.getCurrentPage());
    metadataDTO.setItemsPerPage(metadata.getItemsPerPage());
    metadataDTO.setCurrentItems(metadata.getCurrentItems());
    metadataDTO.setTotalPages(metadata.getTotalPages());
    metadataDTO.setTotalItems(metadata.getTotalItems());

    PaginatedDocumentSearchDTO paginatedDocumentSearchDTO = new PaginatedDocumentSearchDTO();
    paginatedDocumentSearchDTO.setMetadata(metadataDTO);
    paginatedDocumentSearchDTO.setDocuments(
        List.of(documentDTOOne, documentDTOTwo, documentDTOThree));

    when(documentManagementMapper.mapToDocumentSearchDTO(documentSearchFilters, page, size, sort))
        .thenReturn(documentSearchDTO);
    when(documentManagementService.searchDocuments(documentSearchDTO))
        .thenReturn(paginatedDocumentSearchDTO);
    when(documentManagementMapper.mapToPaginatedDocumentSearch(paginatedDocumentSearchDTO))
        .thenReturn(paginatedDocumentSearch);

    ResponseEntity<PaginatedDocumentSearch> response =
        documentManagementControllerImp.searchDocuments(documentSearchFilters, page, size, sort);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(paginatedDocumentSearch, response.getBody());
    verify(documentManagementService, times(1)).searchDocuments(documentSearchDTO);
  }

  @Test
  void searchDocuments_Should_Return200_WithFilterAndReturnOneDocument() {
    Document documentOne = new Document();
    documentOne.setId("1");
    documentOne.setUser("UserOne");
    documentOne.setName("DocumentOne");
    documentOne.setTags(List.of("Tag"));
    documentOne.setSize(50 * 1024 * 10240);
    documentOne.setType(MediaType.APPLICATION_JSON_VALUE);
    documentOne.setCreatedAt(localDateTime);

    Metadata metadata = new Metadata();
    metadata.setCurrentPage(0);
    metadata.setItemsPerPage(3);
    metadata.setCurrentItems(1);
    metadata.setTotalPages(1);
    metadata.setTotalItems(1);

    PaginatedDocumentSearch paginatedDocumentSearch = new PaginatedDocumentSearch();
    paginatedDocumentSearch.setMetadata(metadata);
    paginatedDocumentSearch.setDocuments(List.of(documentOne));

    DocumentSearchFilters documentSearchFilters = new DocumentSearchFilters();
    documentSearchFilters.setUser("UserOne");
    documentSearchFilters.setName("DocumentOne");
    documentSearchFilters.setTags(List.of("Tag"));
    Integer page = 0;
    Integer size = 3;
    String sort = "desc";

    DocumentSearchDTO documentSearchDTO = new DocumentSearchDTO();
    documentSearchDTO.setUser(documentSearchFilters.getUser());
    documentSearchDTO.setName(documentSearchFilters.getName());
    documentSearchDTO.setTags(documentSearchFilters.getTags());
    documentSearchDTO.setPage(page);
    documentSearchDTO.setSize(size);
    documentSearchDTO.setSort(sort);

    DocumentDTO documentDTOOne = new DocumentDTO();
    documentDTOOne.setId(documentOne.getId());
    documentDTOOne.setUser(documentOne.getUser());
    documentDTOOne.setName(documentOne.getName());
    documentDTOOne.setTags(documentOne.getTags());
    documentDTOOne.setSize(documentOne.getSize());
    documentDTOOne.setType(documentDTOOne.getType());
    documentDTOOne.setCreatedAt(documentOne.getCreatedAt());

    MetadataDTO metadataDTO = new MetadataDTO();
    metadataDTO.setCurrentPage(metadata.getCurrentPage());
    metadataDTO.setItemsPerPage(metadata.getItemsPerPage());
    metadataDTO.setCurrentItems(metadata.getCurrentItems());
    metadataDTO.setTotalPages(metadata.getTotalPages());
    metadataDTO.setTotalItems(metadata.getTotalItems());

    PaginatedDocumentSearchDTO paginatedDocumentSearchDTO = new PaginatedDocumentSearchDTO();
    paginatedDocumentSearchDTO.setMetadata(metadataDTO);
    paginatedDocumentSearchDTO.setDocuments(List.of(documentDTOOne));

    when(documentManagementMapper.mapToDocumentSearchDTO(documentSearchFilters, page, size, sort))
        .thenReturn(documentSearchDTO);
    when(documentManagementService.searchDocuments(documentSearchDTO))
        .thenReturn(paginatedDocumentSearchDTO);
    when(documentManagementMapper.mapToPaginatedDocumentSearch(paginatedDocumentSearchDTO))
        .thenReturn(paginatedDocumentSearch);

    ResponseEntity<PaginatedDocumentSearch> response =
        documentManagementControllerImp.searchDocuments(documentSearchFilters, page, size, sort);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(paginatedDocumentSearch, response.getBody());
    verify(documentManagementService, times(1)).searchDocuments(documentSearchDTO);
  }
}
