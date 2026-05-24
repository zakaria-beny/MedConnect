package com.medconnect.teleconsulation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomResponse {
    private String sessionId;
    private String patientId;
    private int position;
    private int estimatedWaitMinutes;
    private LocalDateTime joinedAt;
    private boolean admitted;
    private List<QueueEntry> queue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueueEntry {
        private String patientId;
        private int position;
        private LocalDateTime joinedAt;
    }
}
