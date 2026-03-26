package com.clara.ops.challenge.document_management_service_challenge.domain.mapper;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.DocumentResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.MetadataResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentTag;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Service
public class DocumentMapper {

    public Document toDocument(UploadDocumentRequest uploadDocument, MultipartFile file) {
        return Document.builder()
                .username(uploadDocument.getUser())
                .filename(uploadDocument.getName())
                .filePath(uploadDocument.getFilePath())
                .fileSize(BigInteger.valueOf(file.getSize()))
                .fileType(file.getContentType())
                .createdAt(ZonedDateTime.now())
                .build();
    }

    public Set<DocumentTag> toDocumentTags(UploadDocumentRequest uploadDocument, Document doc) {
        return uploadDocument.getTags().stream()
                .map(tag ->
                        DocumentTag.builder()
                                .document(doc)
                                .tagName(tag)
                                .build())
                .collect(toSet());
    }

    public PaginatedDocumentSearchResponse toPaginatedDocumentSearchResponse(Page<Document> documents) {
        return PaginatedDocumentSearchResponse.builder()
                .metadata(toMetadataResponse(documents))
                .document(
                        documents.get()
                                .map(this::toDocumentResponse)
                                .toList()
                )
                .build();
    }

    public MetadataResponse toMetadataResponse(Page<Document> documents) {
        return MetadataResponse.builder()
                .currentPage(documents.getPageable().getPageNumber())
                .itemsPerPage(documents.getPageable().getPageSize())
                .currentItems(documents.getNumberOfElements())
                .totalPages(documents.getTotalPages())
                .totalItems((int) documents.getTotalElements())
                .build();
    }

    public DocumentResponse toDocumentResponse(Document doc) {
        return DocumentResponse.builder()
                .id(doc.getId().toString())
                .user(doc.getUsername())
                .name(doc.getFilename())
                .size(doc.getFileSize())
                .type(doc.getFileType())
                .tags(doc.getDocumentTags().stream().map(DocumentTag::getTagName).toList())
                .createdAt(doc.getCreatedAt().toString())
                .build();
    }

}
