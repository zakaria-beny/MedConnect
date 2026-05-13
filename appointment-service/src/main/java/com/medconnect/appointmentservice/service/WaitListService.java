package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.request.WaitListRequest;
import com.medconnect.appointmentservice.model.WaitList;
import com.medconnect.appointmentservice.repository.WaitListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing the appointment waitlist.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WaitListService {

    private final WaitListRepository waitListRepository;

    public void addToWaitList(WaitListRequest request) {
        log.info("Adding patient {} to wait list for doctor {}", request.getPatientId(), request.getDoctorId());
        
        WaitList waitList = WaitList.builder()
                .id(UUID.randomUUID().toString())
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .requestedDate(request.getRequestedDate())
                .addedAt(LocalDateTime.now())
                .notified(false)
                .build();

        waitListRepository.save(waitList);
        log.info("Patient added to wait list: {}", request.getPatientId());
    }

    public int getWaitListPosition(String patientId, String doctorId) {
        log.info("Fetching wait list position for patient {} with doctor {}", patientId, doctorId);
        
        List<WaitList> waitList = waitListRepository.findByDoctorIdOrderByAddedAtAsc(doctorId);
        
        int position = 0;
        for (WaitList entry : waitList) {
            position++;
            if (entry.getPatientId().equals(patientId)) {
                log.debug("Patient {} is at position {} in wait list", patientId, position);
                return position;
            }
        }

        log.warn("Patient {} not found in wait list for doctor {}", patientId, doctorId);
        return -1;
    }

    public void removeFromWaitList(String patientId, String doctorId) {
        log.info("Removing patient {} from wait list for doctor {}", patientId, doctorId);
        
        waitListRepository.findByPatientIdAndDoctorId(patientId, doctorId)
                .ifPresent(waitListRepository::delete);
        
        log.info("Patient removed from wait list: {}", patientId);
    }
}
