package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.DocumentRequest;
import com.mediconnect.dmp.dto.response.DocumentResponse;
import com.mediconnect.dmp.model.PatientDocument;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocumentMapper {

    public PatientDocument toModel(DocumentRequest request, String patientId) {
        return PatientDocument.builder()
                .patientId(patientId)
                .documentName(request.getDocumentName())
                .documentType(request.getDocumentType())
                .fileUrl(request.getFileUrl())
                .mimeType(request.getMimeType())
                .fileSize(request.getFileSize())
                .uploadedBy(request.getUploadedBy())
                .description(request.getDescription())
                .virusScanned(false)
                .build();
    }

    public DocumentResponse toResponse(PatientDocument document) {
        return toResponse(document, true);
    }

    public DocumentResponse toMetadataResponse(PatientDocument document) {
        return toResponse(document, false);
    }

    private DocumentResponse toResponse(PatientDocument document, boolean includeFileUrl) {
        return DocumentResponse.builder()
                .id(document.getId())
                .patientId(document.getPatientId())
                .documentName(document.getDocumentName())
                .documentType(document.getDocumentType())
                .fileUrl(includeFileUrl ? document.getFileUrl() : null)
                .mimeType(document.getMimeType())
                .fileSize(document.getFileSize())
                .uploadedBy(document.getUploadedBy())
                .description(document.getDescription())
                .virusScanned(document.isVirusScanned())
                .createdAt(document.getCreatedAt())
                .build();
    }

    public List<DocumentResponse> toResponseList(List<PatientDocument> documents) {
        return documents.stream()
                .map(this::toMetadataResponse)
                .collect(Collectors.toList());
    }
}
