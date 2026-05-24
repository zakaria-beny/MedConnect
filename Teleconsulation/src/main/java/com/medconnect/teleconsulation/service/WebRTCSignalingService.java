package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.model.SessionEvent;
import com.medconnect.teleconsulation.repository.SessionEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Manages WebRTC signaling state. In production, SDP offers/answers and ICE candidates
 * are exchanged via a WebSocket signaling server (e.g. using TURN/STUN servers).
 */
@Service
@RequiredArgsConstructor
public class WebRTCSignalingService {

    private final SessionEventRepository sessionEventRepository;

    public Map<String, Object> handleSDPOffer(String sessionId, String sdpData) {
        logEvent(sessionId, "SDP_OFFER_RECEIVED",
                "SDP offer received: " + sdpData.substring(0, Math.min(60, sdpData.length())) + "...");
        return Map.of(
                "type", "answer",
                "sessionId", sessionId,
                "sdp", "v=0\r\no=- 0 0 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n",
                "status", "answer_ready"
        );
    }

    public Map<String, Object> handleICECandidate(String sessionId, String iceCandidate) {
        logEvent(sessionId, "ICE_CANDIDATE_RECEIVED", "ICE candidate: " + iceCandidate);
        return Map.of("sessionId", sessionId, "status", "candidate_accepted");
    }

    public Map<String, Object> negotiate(String sessionId) {
        logEvent(sessionId, "NEGOTIATION_STARTED", "P2P connection negotiation initiated");
        return Map.of(
                "sessionId", sessionId,
                "status", "negotiating",
                "iceServers", List.of(
                        Map.of("urls", "stun:stun.l.google.com:19302"),
                        Map.of("urls", "stun:stun1.l.google.com:19302")
                )
        );
    }

    public Map<String, Object> handleConnectionFailure(String sessionId) {
        logEvent(sessionId, "CONNECTION_FAILURE", "Connection failure detected, initiating reconnect");
        return Map.of("sessionId", sessionId, "status", "reconnecting",
                "message", "Attempting to re-establish P2P connection");
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
