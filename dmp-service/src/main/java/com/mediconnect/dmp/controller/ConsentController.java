package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.ConsentRequest;
import com.mediconnect.dmp.dto.response.ConsentResponse;
import com.mediconnect.dmp.service.ConsentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService consentService;

    @PutMapping
    public ResponseEntity<ApiResponse<ConsentResponse>> grantAccess(
            @PathVariable String patientId,
            @Valid @RequestBody ConsentRequest request) {
        log.info("PUT /consent for patient {}", patientId);
        ConsentResponse response = consentService.grantAccess(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Access granted successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConsentResponse>>> getConsents(@PathVariable String patientId) {
        log.info("GET /consent for patient {}", patientId);
        List<ConsentResponse> consents = consentService.getConsents(patientId);
        return ResponseEntity.ok(ApiResponse.success("Consents retrieved successfully", consents));
    }

    @DeleteMapping("/{doctorId}")
    public ResponseEntity<ApiResponse<Object>> revokeAccess(
            @PathVariable String patientId,
            @PathVariable String doctorId,
            @RequestParam(required = false, defaultValue = "Revoked by patient") String reason) {
        log.info("DELETE /consent/{} for patient {}", doctorId, patientId);
        consentService.revokeAccess(patientId, doctorId, reason);
        return ResponseEntity.ok(ApiResponse.success("Access revoked successfully"));
    }

    @GetMapping("/verify/{doctorId}")
    public ResponseEntity<ApiResponse<Boolean>> verifyAccess(
            @PathVariable String patientId,
            @PathVariable String doctorId) {
        log.info("GET /consent/verify/{} for patient {}", doctorId, patientId);
        boolean hasAccess = consentService.verifyAccess(patientId, doctorId);
        return ResponseEntity.ok(ApiResponse.success("Access verification completed", hasAccess));
    }
}
