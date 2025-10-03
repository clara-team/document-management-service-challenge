package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto;

import java.math.BigInteger;
import java.time.Instant;
import lombok.Builder;

@Builder
public record UploadDocumentResponse(
    BigInteger documentId,
    String documentUrl,
    String documentName,
    long fileSize,
    Instant uploadDate) {}
