package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.DocumentRequest;
import com.mediconnect.dmp.dto.response.DocumentResponse;
import com.mediconnect.dmp.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping
    public ResponseEntity<ApiResponse<DocumentResponse>> uploadDocument(
            @PathVariable String patientId,
            @Valid @RequestBody DocumentRequest request) {
        log.info("POST /documents for patient {}", patientId);
        DocumentResponse response = documentService.uploadDocument(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocuments(@PathVariable String patientId) {
        log.info("GET /documents for patient {}", patientId);
        List<DocumentResponse> documents = documentService.getDocuments(patientId);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(
            @PathVariable String patientId,
            @PathVariable String documentId) {
        log.info("GET /documents/{} for patient {}", documentId, patientId);
        DocumentResponse document = documentService.getDocumentById(patientId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document retrieved successfully", document));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Object>> deleteDocument(
            @PathVariable String patientId,
            @PathVariable String documentId) {
        log.info("DELETE /documents/{} for patient {}", documentId, patientId);
        documentService.deleteDocument(patientId, documentId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully"));
    }

    @GetMapping("/by-type")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByType(
            @PathVariable String patientId,
            @RequestParam String documentType) {
        log.info("GET /documents/by-type?documentType={} for patient {}", documentType, patientId);
        List<DocumentResponse> documents = documentService.getDocumentsByType(patientId, documentType);
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", documents));
    }
}
