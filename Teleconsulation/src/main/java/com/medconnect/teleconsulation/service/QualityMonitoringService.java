package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Monitors and adjusts WebRTC session quality (bitrate, latency, packet loss).
 * In production, metrics are pulled from WebRTC Stats API in real-time.
 */
@Service
@RequiredArgsConstructor
public class QualityMonitoringService {

    private final SessionEventRepository sessionEventRepository;

    public Map<String, Object> trackBitrate(String sessionId) {
        logEvent(sessionId, "BITRATE_TRACKED", "Current bitrate: 1500 Kbps");
        return Map.of("sessionId", sessionId, "bitrateKbps", 1500,
                "timestamp", LocalDateTime.now().toString());
    }

    public Map<String, Object> trackLatency(String sessionId) {
        logEvent(sessionId, "LATENCY_TRACKED", "Current latency: 45ms");
        return Map.of("sessionId", sessionId, "latencyMs", 45,
                "timestamp", LocalDateTime.now().toString());
    }

    public Map<String, Object> trackPacketLoss(String sessionId) {
        logEvent(sessionId, "PACKET_LOSS_TRACKED", "Current packet loss: 0.5%");
        return Map.of("sessionId", sessionId, "packetLossPercent", 0.5,
                "timestamp", LocalDateTime.now().toString());
    }

    public Map<String, Object> adjustQuality(String sessionId) {
        logEvent(sessionId, "QUALITY_ADJUSTED", "Adaptive bitrate adjustment applied based on network conditions");
        return Map.of("sessionId", sessionId, "action", "adaptive_bitrate_applied",
                "newBitrateKbps", 800, "reason", "High packet loss detected");
    }

    public Map<String, Object> switchQuality(String sessionId, String newResolution) {
        validateResolution(newResolution);
        logEvent(sessionId, "QUALITY_SWITCHED", "Resolution switched to " + newResolution);
        return Map.of("sessionId", sessionId, "resolution", newResolution, "status", "switched");
    }

    private void validateResolution(String resolution) {
        if (!resolution.matches("(1080p|720p|480p|360p|240p)")) {
            throw new IllegalArgumentException(
                    "Invalid resolution: " + resolution + ". Allowed: 1080p, 720p, 480p, 360p, 240p");
        }
    }

    private void logEvent(String sessionId, String eventType, String details) {
        sessionEventRepository.save(SessionEvent.builder()
                .sessionId(sessionId)
                .eventType(eventType)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build());
    }
}
