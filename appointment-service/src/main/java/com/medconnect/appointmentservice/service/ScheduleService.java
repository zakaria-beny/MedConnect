package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.request.ScheduleRequest;
import com.medconnect.appointmentservice.dto.request.VacationRequest;
import com.medconnect.appointmentservice.dto.response.ScheduleResponse;
import com.medconnect.appointmentservice.dto.response.VacationResponse;
import com.medconnect.appointmentservice.exception.ScheduleNotFoundException;
import com.medconnect.appointmentservice.model.DoctorSchedule;
import com.medconnect.appointmentservice.model.VacationPeriod;
import com.medconnect.appointmentservice.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing doctor schedules.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduleService {

    private final DoctorScheduleRepository scheduleRepository;

    public ScheduleResponse setupSchedule(String doctorId, ScheduleRequest request) {
        log.info("Setting up schedule for doctor: {}", doctorId);

        if (scheduleRepository.findByDoctorId(doctorId).isPresent()) {
            throw new IllegalStateException("Schedule already exists for doctor: " + doctorId + ". Use PUT to update.");
        }

        DoctorSchedule schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID().toString())
                .doctorId(doctorId)
                .workDays(request.getWorkDays())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .lunchStart(request.getLunchStart())
                .lunchEnd(request.getLunchEnd())
                .appointmentDurationMinutes(request.getAppointmentDurationMinutes())
                .vacationPeriods(new ArrayList<>())
                .build();
        
        DoctorSchedule saved = scheduleRepository.save(schedule);
        log.info("Schedule created for doctor: {}", doctorId);
        return mapToResponse(saved);
    }

    public ScheduleResponse getSchedule(String doctorId) {
        log.info("Fetching schedule for doctor: {}", doctorId);
        
        DoctorSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for doctor: " + doctorId));
        
        return mapToResponse(schedule);
    }

    public ScheduleResponse updateSchedule(String doctorId, ScheduleRequest request) {
        log.info("Updating schedule for doctor: {}", doctorId);
        
        DoctorSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for doctor: " + doctorId));
        
        schedule.setWorkDays(request.getWorkDays());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setLunchStart(request.getLunchStart());
        schedule.setLunchEnd(request.getLunchEnd());
        schedule.setAppointmentDurationMinutes(request.getAppointmentDurationMinutes());
        
        DoctorSchedule updated = scheduleRepository.save(schedule);
        log.info("Schedule updated for doctor: {}", doctorId);
        return mapToResponse(updated);
    }

    public void addVacationPeriod(String doctorId, VacationRequest request) {
        log.info("Adding vacation period for doctor: {}", doctorId);
        
        DoctorSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for doctor: " + doctorId));
        
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Vacation end date must be on or after start date");
        }

        VacationPeriod vacation = VacationPeriod.builder()
                .id(UUID.randomUUID().toString())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .build();
        
        schedule.getVacationPeriods().add(vacation);
        scheduleRepository.save(schedule);
        log.info("Vacation period added for doctor: {}", doctorId);
    }

    public void removeVacationPeriod(String doctorId, String vacationId) {
        log.info("Removing vacation period for doctor: {}", doctorId);
        
        DoctorSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for doctor: " + doctorId));
        
        schedule.getVacationPeriods().removeIf(v -> v.getId().equals(vacationId));
        scheduleRepository.save(schedule);
        log.info("Vacation period removed for doctor: {}", doctorId);
    }

    private ScheduleResponse mapToResponse(DoctorSchedule schedule) {
        List<VacationResponse> vacations = schedule.getVacationPeriods().stream()
                .map(v -> VacationResponse.builder()
                        .id(v.getId())
                        .startDate(v.getStartDate())
                        .endDate(v.getEndDate())
                        .reason(v.getReason())
                        .build())
                .collect(Collectors.toList());
        
        return ScheduleResponse.builder()
                .id(schedule.getId())
                .doctorId(schedule.getDoctorId())
                .workDays(schedule.getWorkDays())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .lunchStart(schedule.getLunchStart())
                .lunchEnd(schedule.getLunchEnd())
                .appointmentDurationMinutes(schedule.getAppointmentDurationMinutes())
                .vacationPeriods(vacations)
                .build();
    }
}
