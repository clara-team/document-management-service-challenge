package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.DocumentSearchFiltersRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocumentRequest;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.DocumentDownloadUrlResponse;
import com.clara.ops.challenge.document_management_service_challenge.controller.response.PaginatedDocumentSearchResponse;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentTag;
import com.clara.ops.challenge.document_management_service_challenge.domain.mapper.DocumentMapper;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.IDocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.IDocumentTagRepository;
import com.clara.ops.challenge.document_management_service_challenge.exception.BusinessException;
import com.clara.ops.challenge.document_management_service_challenge.infrastructure.MinioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final IDocumentRepository repository;
    private final IDocumentTagRepository tagRepository;
    private final MinioService minioService;
    private final DocumentMapper mapper;

    @Transactional
    public void uploadDocument(MultipartFile file, UploadDocumentRequest uploadDocument) {
        minioService.uploadFile(file, uploadDocument.getFilePath());

        Document doc = mapper.toDocument(uploadDocument, file);
        repository.save(doc);

        Set<DocumentTag> tags = mapper.toDocumentTags(uploadDocument, doc);
        tagRepository.saveAll(tags);
    }

    public PaginatedDocumentSearchResponse searchDocuments(
            Integer page, Integer size, List<String> sort,
            DocumentSearchFiltersRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size, createSort(sort));
        Page<Document> documents = repository.searchByFilters(request.getUser(), request.getName(), request.getTags(), pageable);

        return mapper.toPaginatedDocumentSearchResponse(documents);
    }

    public DocumentDownloadUrlResponse getDocumentDownloadUrl(Integer documentId) {
        String url = repository.findById(documentId)
                .map(Document::getFilePath)
                .map(minioService::generatePresignedUrl)
                .orElseThrow(() -> new BusinessException("Document not found"));
        return new DocumentDownloadUrlResponse(url);
    }

     // TODO sort order não está funcionando
    private Sort createSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) {
            return Sort.by("createdAt").descending();
        }
        return Sort.by(sort.stream()
                .map(Sort.Order::by)
                .toList());
    }


}
