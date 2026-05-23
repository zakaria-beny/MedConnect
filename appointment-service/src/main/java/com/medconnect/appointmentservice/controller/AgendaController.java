package com.medconnect.appointmentservice.controller;

import com.medconnect.appointmentservice.dto.request.ScheduleRequest;
import com.medconnect.appointmentservice.dto.request.VacationRequest;
import com.medconnect.appointmentservice.dto.response.ScheduleResponse;
import com.medconnect.appointmentservice.dto.response.SlotResponse;
import com.medconnect.appointmentservice.service.AvailabilityService;
import com.medconnect.appointmentservice.service.ScheduleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for doctor agenda: schedule management and vacation periods.
 */
@RestController
@RequestMapping("/api/agenda")
@RequiredArgsConstructor
@Validated
public class AgendaController {

    private final ScheduleService scheduleService;
    private final AvailabilityService availabilityService;

    /**
     * GET /api/agenda/{doctorId}/slots?date=2026-05-10
     */
    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<List<SlotResponse>> getAvailableSlots(
            @PathVariable String doctorId,
            @RequestParam @FutureOrPresent(message = "Slot date cannot be in the past") LocalDate date) {
        List<SlotResponse> slots = availabilityService.getAvailableSlots(doctorId, date);
        return ResponseEntity.ok(slots);
    }

    /**
     * POST /api/agenda/{doctorId}/schedule
     */
    @PostMapping("/{doctorId}/schedule")
    public ResponseEntity<ScheduleResponse> setupSchedule(
            @PathVariable String doctorId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.setupSchedule(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/agenda/{doctorId}/schedule
     */
    @GetMapping("/{doctorId}/schedule")
    public ResponseEntity<ScheduleResponse> getSchedule(@PathVariable String doctorId) {
        ScheduleResponse schedule = scheduleService.getSchedule(doctorId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * PUT /api/agenda/{doctorId}/schedule
     */
    @PutMapping("/{doctorId}/schedule")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable String doctorId,
            @Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = scheduleService.updateSchedule(doctorId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/agenda/{doctorId}/vacation
     */
    @PostMapping("/{doctorId}/vacation")
    public ResponseEntity<Void> addVacation(
            @PathVariable String doctorId,
            @Valid @RequestBody VacationRequest request) {
        scheduleService.addVacationPeriod(doctorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * DELETE /api/agenda/{doctorId}/vacation/{vacationId}
     */
    @DeleteMapping("/{doctorId}/vacation/{vacationId}")
    public ResponseEntity<Void> removeVacation(
            @PathVariable String doctorId,
            @PathVariable String vacationId) {
        scheduleService.removeVacationPeriod(doctorId, vacationId);
        return ResponseEntity.noContent().build();
    }
}
