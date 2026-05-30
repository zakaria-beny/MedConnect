package com.mediconnect.prescription.controller;

import com.mediconnect.prescription.dto.ApiResponse;
import com.mediconnect.prescription.dto.request.DispenseRequest;
import com.mediconnect.prescription.dto.request.PrescriptionRequest;
import com.mediconnect.prescription.dto.request.RefillRequest;
import com.mediconnect.prescription.dto.request.SendToPharmacyRequest;
import com.mediconnect.prescription.dto.request.SignRequest;
import com.mediconnect.prescription.dto.response.DispensationResponse;
import com.mediconnect.prescription.dto.response.PrescriptionResponse;
import com.mediconnect.prescription.dto.response.QRCodeResponse;
import com.mediconnect.prescription.dto.response.RefillHistoryResponse;
import com.mediconnect.prescription.service.DispensationService;
import com.mediconnect.prescription.service.PDFService;
import com.mediconnect.prescription.service.PrescriptionService;
import com.mediconnect.prescription.service.QRCodeService;
import com.mediconnect.prescription.service.RefillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@Slf4j
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final DispensationService dispensationService;
    private final RefillService refillService;
    private final QRCodeService qrCodeService;
    private final PDFService pdfService;

    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponse>> createPrescription(@Valid @RequestBody PrescriptionRequest request) {
        log.info("Creating prescription from controller");
        PrescriptionResponse response = prescriptionService.createPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Prescription created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> getPrescription(@PathVariable String id) {
        log.info("Fetching prescription {} from controller", id);
        PrescriptionResponse response = prescriptionService.getPrescription(id);
        return ResponseEntity.ok(ApiResponse.success("Prescription fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> updatePrescription(
            @PathVariable String id,
            @Valid @RequestBody PrescriptionRequest request) {
        log.info("Updating prescription {} from controller", id);
        PrescriptionResponse response = prescriptionService.updatePrescription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Prescription updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deletePrescription(@PathVariable String id) {
        log.info("Deleting prescription {} from controller", id);
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.success("Prescription deleted successfully"));
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> signPrescription(
            @PathVariable String id,
            @Valid @RequestBody SignRequest request) throws Exception {
        log.info("Signing prescription {} from controller", id);
        PrescriptionResponse response = prescriptionService.signPrescription(id, request);
        return ResponseEntity.ok(ApiResponse.success("Prescription signed successfully", response));
    }

    @PostMapping("/{id}/send-to-pharmacy")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> sendToPharmacy(
            @PathVariable String id,
            @Valid @RequestBody SendToPharmacyRequest request) {
        log.info("Sending prescription {} to pharmacy from controller", id);
        PrescriptionResponse response = prescriptionService.sendToPharmacy(id, request);
        return ResponseEntity.ok(ApiResponse.success("Prescription sent to pharmacy successfully", response));
    }

    @PostMapping("/{id}/dispense")
    public ResponseEntity<ApiResponse<DispensationResponse>> dispenseMedication(
            @PathVariable String id,
            @Valid @RequestBody DispenseRequest request) {
        log.info("Dispensing medication for prescription {} from controller", id);
        DispensationResponse response = dispensationService.dispenseMedication(id, request);
        return ResponseEntity.ok(ApiResponse.success("Medication dispensed successfully", response));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<ApiResponse<QRCodeResponse>> getQRCode(@PathVariable String id) throws Exception {
        log.info("Generating QR code for prescription {} from controller", id);
        String qrCodeBase64 = qrCodeService.generateQRCode(id);
        QRCodeResponse response = QRCodeResponse.builder()
                .prescriptionId(id)
                .qrCodeBase64(qrCodeBase64)
                .generatedAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(ApiResponse.success("QR code generated successfully", response));
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> getPrescriptionPdf(@PathVariable String id) {
        log.info("Generating PDF for prescription {} from controller", id);
        byte[] pdfBytes = pdfService.generatePrescriptionPdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=prescription-" + id + ".pdf")
                .body(pdfBytes);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<String>> getStatus(@PathVariable String id) {
        log.info("Fetching status for prescription {} from controller", id);
        String status = prescriptionService.getStatus(id);
        return ResponseEntity.ok(ApiResponse.success("Status fetched successfully", status));
    }

    @PostMapping("/{id}/refill-request")
    public ResponseEntity<ApiResponse<RefillHistoryResponse>> requestRefill(
            @PathVariable String id,
            @Valid @RequestBody RefillRequest request) {
        log.info("Requesting refill for prescription {} from controller", id);
        RefillHistoryResponse response = refillService.requestRefill(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Refill requested successfully", response));
    }

    @PostMapping("/{id}/refill-approve")
    public ResponseEntity<ApiResponse<RefillHistoryResponse>> approveRefill(
            @PathVariable String id,
            @RequestParam String doctorId) {
        log.info("Approving refill {} from controller", id);
        RefillHistoryResponse response = refillService.approveRefill(id, doctorId);
        return ResponseEntity.ok(ApiResponse.success("Refill approved successfully", response));
    }

    @PostMapping("/{id}/refill-reject")
    public ResponseEntity<ApiResponse<RefillHistoryResponse>> rejectRefill(
            @PathVariable String id,
            @RequestParam String reason) {
        log.info("Rejecting refill {} from controller", id);
        RefillHistoryResponse response = refillService.rejectRefill(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Refill rejected successfully", response));
    }

    @GetMapping("/{id}/refills")
    public ResponseEntity<ApiResponse<List<RefillHistoryResponse>>> getRefillHistory(@PathVariable String id) {
        log.info("Fetching refill history for prescription {} from controller", id);
        List<RefillHistoryResponse> response = refillService.getRefillHistory(id);
        return ResponseEntity.ok(ApiResponse.success("Refill history fetched successfully", response));
    }

    @GetMapping("/{id}/dispensations")
    public ResponseEntity<ApiResponse<List<DispensationResponse>>> getDispensations(@PathVariable String id) {
        log.info("Fetching dispensations for prescription {} from controller", id);
        List<DispensationResponse> response = dispensationService.getDispensations(id);
        return ResponseEntity.ok(ApiResponse.success("Dispensations fetched successfully", response));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsByPatient(@PathVariable String patientId) {
        log.info("Fetching prescriptions for patient {} from controller", patientId);
        List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.success("Prescriptions fetched successfully", response));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsByDoctor(@PathVariable String doctorId) {
        log.info("Fetching prescriptions by doctor {} from controller", doctorId);
        List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.success("Prescriptions fetched successfully", response));
    }

    @GetMapping("/pharmacy/{pharmacyId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponse>>> getPrescriptionsByPharmacy(@PathVariable String pharmacyId) {
        log.info("Fetching prescriptions for pharmacy {} from controller", pharmacyId);
        List<PrescriptionResponse> response = prescriptionService.getPrescriptionsByPharmacy(pharmacyId);
        return ResponseEntity.ok(ApiResponse.success("Prescriptions fetched successfully", response));
    }
}
