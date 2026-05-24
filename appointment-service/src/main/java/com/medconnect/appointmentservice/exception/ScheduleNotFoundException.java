package com.medconnect.appointmentservice.exception;

/**
 * Exception thrown when a doctor's schedule is not found.
 */
public class ScheduleNotFoundException extends RuntimeException {
    public ScheduleNotFoundException(String message) {
        super(message);
    }

    public ScheduleNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
