package com.mediconnect.dmp.service;

import com.mediconnect.dmp.dto.request.HealthNotebookRequest;
import com.mediconnect.dmp.dto.response.HealthNotebookResponse;
import com.mediconnect.dmp.mapper.HealthNotebookMapper;
import com.mediconnect.dmp.model.HealthNotebookEntry;
import com.mediconnect.dmp.repository.HealthNotebookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthNotebookService {

    private final HealthNotebookRepository healthNotebookRepository;
    private final HealthNotebookMapper healthNotebookMapper;

    public HealthNotebookResponse logVitals(String patientId, HealthNotebookRequest request) {
        log.info("Logging vitals for patient {}", patientId);

        HealthNotebookEntry entry = healthNotebookMapper.toModel(request, patientId);
        checkForAbnormalValues(entry);

        HealthNotebookEntry saved = healthNotebookRepository.save(entry);
        return healthNotebookMapper.toResponse(saved);
    }

    public List<HealthNotebookResponse> getEntries(String patientId) {
        log.info("Fetching health notebook entries for patient {}", patientId);
        List<HealthNotebookEntry> entries = healthNotebookRepository.findByPatientIdOrderByMeasuredAtDesc(patientId);
        return healthNotebookMapper.toResponseList(entries);
    }

    public List<HealthNotebookResponse> getTrends(String patientId, LocalDateTime from, LocalDateTime to) {
        log.info("Fetching health notebook trends for patient {} from {} to {}", patientId, from, to);
        List<HealthNotebookEntry> entries = healthNotebookRepository.findByPatientIdAndMeasuredAtBetweenOrderByMeasuredAtAsc(patientId, from, to);
        return healthNotebookMapper.toResponseList(entries);
    }

    public List<HealthNotebookResponse> getAlerts(String patientId) {
        log.info("Fetching health notebook alerts for patient {}", patientId);
        List<HealthNotebookEntry> entries = healthNotebookRepository.findByPatientIdAndFlaggedTrue(patientId);
        return healthNotebookMapper.toResponseList(entries);
    }

    private void checkForAbnormalValues(HealthNotebookEntry entry) {
        List<String> alerts = new ArrayList<>();

        if (entry.getBloodPressureSystolic() != null) {
            if (entry.getBloodPressureSystolic() >= 180) {
                alerts.add("CRITICAL: Hypertensive crisis");
            } else if (entry.getBloodPressureSystolic() >= 130) {
                alerts.add("HIGH: Hypertension");
            } else if (entry.getBloodPressureSystolic() < 90) {
                alerts.add("LOW: Hypotension");
            }
        }

        if (entry.getBloodGlucose() != null) {
            if (entry.getBloodGlucose() < 70) {
                alerts.add("LOW: Hypoglycemia");
            } else if (entry.getBloodGlucose() > 250) {
                alerts.add("HIGH: Hyperglycemia");
            }
        }

        if (entry.getHeartRate() != null) {
            if (entry.getHeartRate() < 50) {
                alerts.add("LOW: Bradycardia");
            } else if (entry.getHeartRate() > 120) {
                alerts.add("HIGH: Tachycardia");
            }
        }

        if (entry.getOxygenSaturation() != null && entry.getOxygenSaturation() < 90) {
            alerts.add("CRITICAL: Hypoxia");
        }

        if (entry.getTemperature() != null) {
            if (entry.getTemperature() >= 38.5) {
                alerts.add("HIGH: Fever");
            } else if (entry.getTemperature() < 36.0) {
                alerts.add("LOW: Hypothermia");
            }
        }

        if (!alerts.isEmpty()) {
            entry.setFlagged(true);
            entry.setAlertMessage(String.join(" | ", alerts));
            log.warn("Abnormal values detected for patient {}: {}", entry.getPatientId(), entry.getAlertMessage());
        }
    }
}
