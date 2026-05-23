package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.dto.request.ImagingResultRequest;
import com.mediconnect.dmp.dto.response.ImagingResultResponse;
import com.mediconnect.dmp.service.ImagingResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/imaging")
@RequiredArgsConstructor
public class ImagingController {

    private final ImagingResultService imagingResultService;

    @PostMapping
    public ResponseEntity<ApiResponse<ImagingResultResponse>> addImagingStudy(
            @PathVariable String patientId,
            @Valid @RequestBody ImagingResultRequest request) {
        log.info("POST /imaging for patient {}", patientId);
        ImagingResultResponse response = imagingResultService.addImagingStudy(patientId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Imaging study added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ImagingResultResponse>>> getImagingResults(@PathVariable String patientId) {
        log.info("GET /imaging for patient {}", patientId);
        List<ImagingResultResponse> imagingResults = imagingResultService.getImagingResults(patientId);
        return ResponseEntity.ok(ApiResponse.success("Imaging results retrieved successfully", imagingResults));
    }

    @GetMapping("/{imagingId}")
    public ResponseEntity<ApiResponse<ImagingResultResponse>> getImagingResultById(
            @PathVariable String patientId,
            @PathVariable String imagingId) {
        log.info("GET /imaging/{} for patient {}", imagingId, patientId);
        ImagingResultResponse imagingResult = imagingResultService.getImagingResultById(patientId, imagingId);
        return ResponseEntity.ok(ApiResponse.success("Imaging result retrieved successfully", imagingResult));
    }

    @PutMapping("/{imagingId}")
    public ResponseEntity<ApiResponse<ImagingResultResponse>> updateImagingResult(
            @PathVariable String patientId,
            @PathVariable String imagingId,
            @Valid @RequestBody ImagingResultRequest request) {
        log.info("PUT /imaging/{} for patient {}", imagingId, patientId);
        ImagingResultResponse imagingResult = imagingResultService.updateImagingResult(patientId, imagingId, request);
        return ResponseEntity.ok(ApiResponse.success("Imaging result updated successfully", imagingResult));
    }

    @GetMapping("/by-type")
    public ResponseEntity<ApiResponse<List<ImagingResultResponse>>> getImagingResultsByType(
            @PathVariable String patientId,
            @RequestParam String type) {
        log.info("GET /imaging/by-type?type={} for patient {}", type, patientId);
        List<ImagingResultResponse> imagingResults = imagingResultService.getImagingResultsByType(patientId, type);
        return ResponseEntity.ok(ApiResponse.success("Imaging results retrieved successfully", imagingResults));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<List<ImagingResultResponse>>> getImagingResultsByDateRange(
            @PathVariable String patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("GET /imaging/by-date-range from {} to {} for patient {}", startDate, endDate, patientId);
        List<ImagingResultResponse> imagingResults = imagingResultService.getImagingResultsByDateRange(patientId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success("Imaging results retrieved successfully", imagingResults));
    }
}
