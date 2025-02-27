package com.clara.ops.challenge.document_management_service_challenge;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RestController("/document-management")
public class DocumentManagementController {

    @PostMapping("/upload")
    public void uploadDocument() {

    }

    @GetMapping("/search")
    public void searchDocuments() {

    }

    @GetMapping("/download")
    public void downloadDocument() {

    }
}
