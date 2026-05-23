package com.mediconnect.dmp.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * WHY a standard response wrapper?
 * Instead of returning raw objects, we always wrap them in this class.
 * This way, every API response has the same predictable structure:
 *
 * {
 *   "success": true,
 *   "message": "Allergy added successfully",
 *   "data": { ...the actual allergy object... },
 *   "timestamp": "2026-05-06T10:30:00"
 * }
 *
 * If there's an error:
 * {
 *   "success": false,
 *   "message": "Allergy not found",
 *   "data": null,
 *   "timestamp": "2026-05-06T10:30:00"
 * }
 *
 * @JsonInclude(NON_NULL) = don't include null fields in JSON output
 * This keeps the response clean.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Convenience static methods so we can write:
    // return ApiResponse.success("Created", allergy);
    // instead of writing the builder every time
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}