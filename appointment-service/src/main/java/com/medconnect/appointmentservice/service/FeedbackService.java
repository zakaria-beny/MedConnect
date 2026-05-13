package com.medconnect.appointmentservice.service;

import com.medconnect.appointmentservice.dto.request.FeedbackRequest;
import com.medconnect.appointmentservice.dto.response.FeedbackResponse;
import com.medconnect.appointmentservice.exception.AppointmentNotFoundException;
import com.medconnect.appointmentservice.exception.InvalidRatingException;
import com.medconnect.appointmentservice.model.Appointment;
import com.medconnect.appointmentservice.model.AppointmentFeedback;
import com.medconnect.appointmentservice.repository.AppointmentFeedbackRepository;
import com.medconnect.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing appointment feedback and ratings.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final AppointmentFeedbackRepository feedbackRepository;
    private final AppointmentRepository appointmentRepository;

    public FeedbackResponse submitFeedback(String appointmentId, FeedbackRequest request) {
        log.info("Submitting feedback for appointment {}", appointmentId);

        if (request.getRating() < 1 || request.getRating() > 5) {
            log.error("Invalid rating: {}. Must be between 1 and 5.", request.getRating());
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> {
                    log.error("Appointment not found: {}", appointmentId);
                    return new AppointmentNotFoundException("Appointment not found with id: " + appointmentId);
                });

        AppointmentFeedback feedback = AppointmentFeedback.builder()
                .id(UUID.randomUUID().toString())
                .appointmentId(appointmentId)
                .patientId(appointment.getPatientId())
                .doctorId(appointment.getDoctorId())
                .rating(request.getRating())
                .comments(request.getComments())
                .submittedAt(LocalDateTime.now())
                .build();

        AppointmentFeedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Feedback submitted for appointment {}: rating={}", appointmentId, request.getRating());

        return FeedbackResponse.builder()
                .appointmentId(appointmentId)
                .rating(savedFeedback.getRating())
                .comments(savedFeedback.getComments())
                .submittedAt(savedFeedback.getSubmittedAt())
                .build();
    }

    public double getAverageRating(String doctorId) {
        log.info("Fetching average rating for doctor {}", doctorId);

        List<AppointmentFeedback> feedbacks = feedbackRepository.findByDoctorId(doctorId);

        if (feedbacks.isEmpty()) {
            log.warn("No feedback found for doctor {}", doctorId);
            return 0.0;
        }

        double average = feedbacks.stream()
                .mapToInt(AppointmentFeedback::getRating)
                .average()
                .orElse(0.0);

        log.debug("Average rating for doctor {}: {}", doctorId, average);
        return average;
    }
}
