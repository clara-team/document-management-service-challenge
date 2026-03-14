package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DocumentDetailResponseDto {
    private UUID id;
    private String user;
    private String name;
    private List<String> tags;
    private Long size;
    private String type;
    private LocalDateTime createdAt;
}
