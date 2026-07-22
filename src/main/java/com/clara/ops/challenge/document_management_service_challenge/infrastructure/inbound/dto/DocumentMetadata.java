package com.clara.ops.challenge.document_management_service_challenge.infrastructure.inbound.dto;

import java.util.ArrayList;

/**
 * Represents metadata information associated with a document. This metadata includes user-specific
 * identifiers and tags that categorize the document.
 *
 * <p>This record provides an immutable data structure to store and access: - Tags associated with
 * the document, represented as a list of strings. - The user ID of the individual who created or
 * owns the document.
 *
 * <p>It is designed to offer a simple structure for documents' metadata representation.
 */
public record DocumentMetadata(ArrayList<String> tags, String userId) {}
