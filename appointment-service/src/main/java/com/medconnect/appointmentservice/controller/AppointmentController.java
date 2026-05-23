package com.medconnect.appointmentservice.controller;

import com.medconnect.appointmentservice.dto.request.BookAppointmentRequest;
import com.medconnect.appointmentservice.dto.request.FeedbackRequest;
import com.medconnect.appointmentservice.dto.request.RescheduleRequest;
import com.medconnect.appointmentservice.dto.response.AppointmentResponse;
import com.medconnect.appointmentservice.dto.response.CheckInResponse;
import com.medconnect.appointmentservice.dto.response.FeedbackResponse;
import com.medconnect.appointmentservice.service.AppointmentService;
import com.medconnect.appointmentservice.service.CheckInService;
import com.medconnect.appointmentservice.service.FeedbackService;
import com.medconnect.appointmentservice.service.NoShowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for core appointment CRUD, workflow and feedback endpoints.
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final CheckInService checkInService;
    private final NoShowService noShowService;
    private final FeedbackService feedbackService;

    /**
     * POST /api/appointments
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/appointments/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointment(@PathVariable String id) {
        AppointmentResponse response = appointmentService.getAppointment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /api/appointments/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable String id,
            @Valid @RequestBody RescheduleRequest request) {
        AppointmentResponse response = appointmentService.rescheduleAppointment(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/appointments/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelAppointment(
            @PathVariable String id,
            @RequestParam(required = false) String reason) {
        appointmentService.cancelAppointment(id, reason);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/appointments/patient/{patientId}
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(@PathVariable String patientId) {
        List<AppointmentResponse> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * GET /api/appointments/doctor/{doctorId}
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(@PathVariable String doctorId) {
        List<AppointmentResponse> appointments = appointmentService.getDoctorAppointments(doctorId);
        return ResponseEntity.ok(appointments);
    }

    /**
     * POST /api/appointments/{id}/check-in
     */
    @PostMapping("/{id}/check-in")
    public ResponseEntity<CheckInResponse> checkInPatient(@PathVariable String id) {
        CheckInResponse response = checkInService.checkInPatient(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/appointments/{id}/queue-position
     */
    @GetMapping("/{id}/queue-position")
    public ResponseEntity<CheckInResponse> getQueuePosition(@PathVariable String id) {
        CheckInResponse response = checkInService.getQueuePosition(id);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/appointments/{id}/no-show
     */
    @PostMapping("/{id}/no-show")
    public ResponseEntity<Void> markNoShow(@PathVariable String id) {
        noShowService.markNoShow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/appointments/{id}/feedback
     */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<FeedbackResponse> submitFeedback(
            @PathVariable String id,
            @Valid @RequestBody FeedbackRequest request) {
        FeedbackResponse response = feedbackService.submitFeedback(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
