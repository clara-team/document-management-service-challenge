package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto;

public record TokenResponse(String token, String type, long expiresIn) {}
