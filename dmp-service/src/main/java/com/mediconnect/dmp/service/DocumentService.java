package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.DocumentRequest;
import com.mediconnect.dmp.dto.response.DocumentResponse;
import com.mediconnect.dmp.exception.ResourceNotFoundException;
import com.mediconnect.dmp.mapper.DocumentMapper;
import com.mediconnect.dmp.model.PatientDocument;
import com.mediconnect.dmp.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentMapper documentMapper;

    public DocumentResponse uploadDocument(String patientId, DocumentRequest request) {
        log.info("Uploading document for patient {}", patientId);
        PatientDocument document = documentMapper.toModel(request, patientId);
        PatientDocument saved = documentRepository.save(document);
        return documentMapper.toResponse(saved);
    }

    public List<DocumentResponse> getDocuments(String patientId) {
        List<PatientDocument> documents = documentRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        return documentMapper.toResponseList(documents);
    }

    public DocumentResponse getDocumentById(String patientId, String documentId) {
        PatientDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        return documentMapper.toResponse(document);
    }

    public void deleteDocument(String patientId, String documentId) {
        PatientDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", documentId));
        documentRepository.deleteById(documentId);
    }

    public List<DocumentResponse> getDocumentsByType(String patientId, String documentType) {
        List<PatientDocument> documents = documentRepository.findByPatientIdAndDocumentType(patientId, documentType);
        return documentMapper.toResponseList(documents);
    }
}