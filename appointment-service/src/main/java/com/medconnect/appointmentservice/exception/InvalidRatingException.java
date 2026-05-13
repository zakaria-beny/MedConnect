package com.medconnect.appointmentservice.exception;

/**
 * Exception thrown when a feedback rating is invalid.
 */
public class InvalidRatingException extends RuntimeException {
    public InvalidRatingException(String message) {
        super(message);
    }

    public InvalidRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
