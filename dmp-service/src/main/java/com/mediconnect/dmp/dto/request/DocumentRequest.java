package com.mediconnect.dmp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotBlank(message = "Document name is required")
    private String documentName;

    @NotBlank(message = "Document type is required")
    private String documentType; // LAB, IMAGING, PRESCRIPTION, OTHER

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "MIME type is required")
    private String mimeType;

    private Long fileSize;

    @NotBlank(message = "Uploaded by is required")
    private String uploadedBy;

    private String description;
}
