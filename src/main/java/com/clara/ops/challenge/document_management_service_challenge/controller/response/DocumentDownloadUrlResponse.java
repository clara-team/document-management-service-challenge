package com.clara.ops.challenge.document_management_service_challenge.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The document download URL.")
public class DocumentDownloadUrlResponse {

    @Schema(description = "The document download URL.")
    private String url;

}
