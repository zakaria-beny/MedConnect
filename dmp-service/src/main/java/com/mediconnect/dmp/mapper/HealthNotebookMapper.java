package com.mediconnect.dmp.mapper;

import com.mediconnect.dmp.dto.request.HealthNotebookRequest;
import com.mediconnect.dmp.dto.response.HealthNotebookResponse;
import com.mediconnect.dmp.model.HealthNotebookEntry;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HealthNotebookMapper {

    public HealthNotebookEntry toModel(HealthNotebookRequest request, String patientId) {
        return HealthNotebookEntry.builder()
                .patientId(patientId)
                .measuredAt(request.getMeasuredAt())
                .bloodPressureSystolic(request.getBloodPressureSystolic())
                .bloodPressureDiastolic(request.getBloodPressureDiastolic())
                .heartRate(request.getHeartRate())
                .bloodGlucose(request.getBloodGlucose())
                .bloodGlucoseUnit(request.getBloodGlucoseUnit())
                .weight(request.getWeight())
                .height(request.getHeight())
                .temperature(request.getTemperature())
                .oxygenSaturation(request.getOxygenSaturation())
                .steps(request.getSteps())
                .patientNotes(request.getPatientNotes())
                .build();
    }

    public HealthNotebookResponse toResponse(HealthNotebookEntry entry) {
        return HealthNotebookResponse.builder()
                .id(entry.getId())
                .patientId(entry.getPatientId())
                .measuredAt(entry.getMeasuredAt())
                .bloodPressureSystolic(entry.getBloodPressureSystolic())
                .bloodPressureDiastolic(entry.getBloodPressureDiastolic())
                .heartRate(entry.getHeartRate())
                .bloodGlucose(entry.getBloodGlucose())
                .bloodGlucoseUnit(entry.getBloodGlucoseUnit())
                .weight(entry.getWeight())
                .height(entry.getHeight())
                .temperature(entry.getTemperature())
                .oxygenSaturation(entry.getOxygenSaturation())
                .steps(entry.getSteps())
                .patientNotes(entry.getPatientNotes())
                .flagged(entry.isFlagged())
                .alertMessage(entry.getAlertMessage())
                .createdAt(entry.getCreatedAt())
                .build();
    }

    public List<HealthNotebookResponse> toResponseList(List<HealthNotebookEntry> entries) {
        return entries.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
