package com.clara.ops.challenge.document_management_service_challenge.infrastructure.outbound.storage;

import com.clara.ops.challenge.document_management_service_challenge.application.port.out.DocumentStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioDocumentStorageAdapter implements DocumentStoragePort {}
