package com.mediconnect.dmp.controller;

import com.mediconnect.dmp.dto.ApiResponse;
import com.mediconnect.dmp.model.AccessLog;
import com.mediconnect.dmp.service.AccessLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/dmp/{patientId}/access-log")
@RequiredArgsConstructor
public class AccessLogController {

    private final AccessLogService accessLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccessLog>>> getAccessHistory(@PathVariable String patientId) {
        log.info("GET /access-log for patient {}", patientId);
        List<AccessLog> history = accessLogService.getAccessHistory(patientId);
        return ResponseEntity.ok(ApiResponse.success("Access history retrieved successfully", history));
    }

    @GetMapping("/unauthorized")
    public ResponseEntity<ApiResponse<List<AccessLog>>> getUnauthorizedAccesses(@PathVariable String patientId) {
        log.info("GET /access-log/unauthorized for patient {}", patientId);
        List<AccessLog> unauthorizedAccesses = accessLogService.getUnauthorizedAccesses(patientId);
        return ResponseEntity.ok(ApiResponse.success("Unauthorized accesses retrieved successfully", unauthorizedAccesses));
    }
}
