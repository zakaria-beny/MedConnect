package com.mediconnect.dmp.service;

import com.mediconnect.dmp.model.AccessLog;
import com.mediconnect.dmp.repository.AccessLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessLogService {

    private final AccessLogRepository accessLogRepository;

    public void logAccess(String patientId, String accessedByUserId, String accessedByUserName, String accessedByUserRole,
                          List<String> sectionsAccessed, String reason, AccessLog.ActionType action, String ipAddress, boolean authorized) {
        log.info("Logging access to patient {} by user {} ({})", patientId, accessedByUserId, accessedByUserName);

        AccessLog accessLog = AccessLog.builder()
                .patientId(patientId)
                .accessedByUserId(accessedByUserId)
                .accessedByUserName(accessedByUserName)
                .accessedByUserRole(accessedByUserRole)
                .sectionsAccessed(sectionsAccessed)
                .reason(reason)
                .action(action)
                .ipAddress(ipAddress)
                .authorized(authorized)
                .build();

        accessLogRepository.save(accessLog);
    }

    public List<AccessLog> getAccessHistory(String patientId) {
        log.info("Fetching access history for patient {}", patientId);
        return accessLogRepository.findByPatientIdOrderByAccessedAtDesc(patientId);
    }

    public List<AccessLog> getUnauthorizedAccesses(String patientId) {
        log.info("Fetching unauthorized accesses for patient {}", patientId);
        return accessLogRepository.findByPatientIdAndAuthorizedFalse(patientId);
    }
}
