package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class DocumentSearchFilters {

    private String user;
    private String name;
    private List<String> tags;
}
