package com.clara.ops.challenge.document_management_service_challenge.domain.dto;

import java.util.List;

/** Data Transfer Object (DTO) for document. */
public record DocumentDto(
    Long id,
    String name,
    List<String> tags,
    String type,
    Long size,
    String path,
    String createdAt) {}
