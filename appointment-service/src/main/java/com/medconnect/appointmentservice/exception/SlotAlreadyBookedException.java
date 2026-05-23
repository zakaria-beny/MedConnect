package com.medconnect.appointmentservice.exception;

/**
 * Exception thrown when an appointment slot is already booked.
 */
public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(String message) {
        super(message);
    }

    public SlotAlreadyBookedException(String message, Throwable cause) {
        super(message, cause);
    }
}
