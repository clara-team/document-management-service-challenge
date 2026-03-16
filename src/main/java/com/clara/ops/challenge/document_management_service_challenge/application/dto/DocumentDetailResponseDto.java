package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentDetailResponseDto(
    UUID id,
    String user,
    String name,
    List<String> tags,
    Long size,
    String type,
    LocalDateTime createdAt) {}
