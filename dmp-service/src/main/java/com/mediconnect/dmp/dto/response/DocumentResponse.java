package com.mediconnect.dmp.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private String id;
    private String patientId;
    private String documentName;
    private String documentType;
    private String fileUrl;
    private String mimeType;
    private Long fileSize;
    private String uploadedBy;
    private String description;
    @JsonProperty("virusScanned")
    private boolean virusScanned;
    private LocalDateTime createdAt;
}
