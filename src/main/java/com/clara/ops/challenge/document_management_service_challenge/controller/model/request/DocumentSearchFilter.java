package com.clara.ops.challenge.document_management_service_challenge.controller.model.request;

import java.util.Set;

public record DocumentSearchFilter(String user, String name, Set<String> tags) {}
