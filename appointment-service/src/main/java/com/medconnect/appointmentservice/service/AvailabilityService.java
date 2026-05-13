package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.response.SlotResponse;
import com.medconnect.appointmentservice.exception.ScheduleNotFoundException;
import com.medconnect.appointmentservice.model.AppointmentSlot;
import com.medconnect.appointmentservice.model.DoctorSchedule;
import com.medconnect.appointmentservice.repository.AppointmentSlotRepository;
import com.medconnect.appointmentservice.repository.DoctorScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing appointment availability and slots.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AvailabilityService {

    private final AppointmentSlotRepository slotRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public List<SlotResponse> getAvailableSlots(String doctorId, LocalDate date) {
        log.info("Fetching available slots for doctor {} on date {}", doctorId, date);

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot query slots for a past date: " + date);
        }

        List<AppointmentSlot> existing = slotRepository.findByDoctorIdAndDate(doctorId, date);
        if (existing.isEmpty()) {
            calculateSlots(doctorId, date);
        }

        List<AppointmentSlot> slots = slotRepository.findByDoctorIdAndDateAndBookedFalse(doctorId, date);

        return slots.stream()
                .map(slot -> SlotResponse.builder()
                        .id(slot.getId())
                        .date(slot.getDate())
                        .startTime(slot.getStartTime())
                        .endTime(slot.getEndTime())
                        .isAvailable(true)
                        .build())
                .collect(Collectors.toList());
    }

    public List<AppointmentSlot> calculateSlots(String doctorId, LocalDate date) {
        log.info("Calculating slots for doctor {} on date {}", doctorId, date);
        
        DoctorSchedule schedule = scheduleRepository.findByDoctorId(doctorId)
                .orElseThrow(() -> new ScheduleNotFoundException("Schedule not found for doctor: " + doctorId));
        
        // Check if date falls in vacation period
        boolean isVacation = schedule.getVacationPeriods().stream()
                .anyMatch(v -> !date.isBefore(v.getStartDate()) && !date.isAfter(v.getEndDate()));
        
        if (isVacation) {
            log.info("Date {} is in vacation period for doctor {}", date, doctorId);
            return new ArrayList<>();
        }

        String dayOfWeek = date.getDayOfWeek().toString();
        if (!schedule.getWorkDays().contains(dayOfWeek)) {
            log.info("Doctor {} does not work on {}", doctorId, dayOfWeek);
            return new ArrayList<>();
        }

        List<AppointmentSlot> slots = new ArrayList<>();
        LocalTime current = LocalTime.parse(schedule.getStartTime(), TIME_FORMATTER);
        LocalTime endTime = LocalTime.parse(schedule.getEndTime(), TIME_FORMATTER);
        LocalTime lunchStart = LocalTime.parse(schedule.getLunchStart(), TIME_FORMATTER);
        LocalTime lunchEnd = LocalTime.parse(schedule.getLunchEnd(), TIME_FORMATTER);
        int duration = schedule.getAppointmentDurationMinutes();

        while (current.plusMinutes(duration).isBefore(endTime) || current.plusMinutes(duration).equals(endTime)) {
            LocalTime slotEnd = current.plusMinutes(duration);
            
            // Skip lunch time
            if (!current.isBefore(lunchStart) && current.isBefore(lunchEnd)) {
                current = lunchEnd;
                continue;
            }

            AppointmentSlot slot = AppointmentSlot.builder()
                    .id(UUID.randomUUID().toString())
                    .doctorId(doctorId)
                    .date(date)
                    .startTime(current.format(TIME_FORMATTER))
                    .endTime(slotEnd.format(TIME_FORMATTER))
                    .booked(false)
                    .build();
            slots.add(slot);
            current = slotEnd;
        }

        slotRepository.saveAll(slots);
        log.info("Generated {} slots for doctor {} on {}", slots.size(), doctorId, date);
        return slots;
    }

    public Optional<SlotResponse> findNextAvailableSlot(String doctorId) {
        log.info("Finding next available slot for doctor: {}", doctorId);

        LocalDate date = LocalDate.now().plusDays(1);
        for (int i = 0; i < 14; i++, date = date.plusDays(1)) {
            try {
                List<AppointmentSlot> existing = slotRepository.findByDoctorIdAndDate(doctorId, date);
                if (existing.isEmpty()) {
                    calculateSlots(doctorId, date);
                }
                List<AppointmentSlot> availableSlots = slotRepository.findByDoctorIdAndDateAndBookedFalse(doctorId, date);
                if (!availableSlots.isEmpty()) {
                    AppointmentSlot slot = availableSlots.get(0);
                    log.info("Found available slot for doctor {} on {}", doctorId, date);
                    return Optional.of(SlotResponse.builder()
                            .id(slot.getId())
                            .date(slot.getDate())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .isAvailable(true)
                            .build());
                }
            } catch (ScheduleNotFoundException e) {
                log.debug("No schedule for doctor {} on {}, skipping", doctorId, date);
            }
        }

        log.warn("No available slots found for doctor {} in next 14 days", doctorId);
        return Optional.empty();
    }
}
