package com.clara.ops.challenge.document_management_service_challenge.service;

import com.clara.ops.challenge.document_management_service_challenge.controller.request.UploadDocument;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.Document;
import com.clara.ops.challenge.document_management_service_challenge.domain.entity.DocumentTag;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.IDocumentRepository;
import com.clara.ops.challenge.document_management_service_challenge.domain.repository.IDocumentTagRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final IDocumentRepository repository;
    private final IDocumentTagRepository tagRepository;

    @Transactional
    public void uploadDocument(UploadDocument uploadDocument) {
        // Conexão com MiniIO e envia o arquivo
        Document doc = Document.builder()
                .username(uploadDocument.getUser())
                .filename(uploadDocument.getName())
//                .filePath("")
//                .fileSize()
//                .fileType()
                .createdAt(ZonedDateTime.now())
                .build();

        repository.save(doc);

        Set<DocumentTag> tags = uploadDocument.getTags().stream()
                .map(tag ->
                        DocumentTag.builder()
                                .document(doc)
                                .tagName(tag)
                                .build())
                .collect(toSet());
        tagRepository.saveAll(tags);
    }

}
