package com.clara.ops.challenge.document_management_service_challenge.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadDocumentRequestDto {

    @NotBlank(message = "user must not be blank")
    private String user;

    @NotBlank(message = "name must not be blank")
    private String name;

    @NotEmpty(message = "tags must not be empty")
    private List<@NotBlank String> tags;
}
