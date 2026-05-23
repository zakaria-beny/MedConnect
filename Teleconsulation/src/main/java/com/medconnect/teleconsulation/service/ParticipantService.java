package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.ParticipantRole;
import com.medconnect.teleconsulation.model.SessionParticipant;
import com.medconnect.teleconsulation.repository.SessionParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final SessionParticipantRepository participantRepository;

    public SessionParticipant addParticipant(String sessionId, String userId, ParticipantRole role) {
        SessionParticipant participant = SessionParticipant.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();
        return participantRepository.save(participant);
    }

    public void removeParticipant(String sessionId, String userId) {
        SessionParticipant participant = participantRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Participant not found: userId=" + userId + " in session=" + sessionId));
        participant.setLeftAt(LocalDateTime.now());
        participantRepository.save(participant);
    }

    public SessionParticipant trackJoinTime(String sessionId, String userId) {
        return participantRepository.findBySessionIdAndUserId(sessionId, userId)
                .map(p -> {
                    p.setJoinedAt(LocalDateTime.now());
                    return participantRepository.save(p);
                })
                .orElseGet(() -> participantRepository.save(
                        SessionParticipant.builder()
                                .sessionId(sessionId)
                                .userId(userId)
                                .joinedAt(LocalDateTime.now())
                                .build()));
    }

    public SessionParticipant trackLeaveTime(String sessionId, String userId) {
        SessionParticipant participant = participantRepository
                .findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Participant not found: userId=" + userId + " in session=" + sessionId));
        participant.setLeftAt(LocalDateTime.now());
        return participantRepository.save(participant);
    }

    public List<SessionParticipant> getParticipants(String sessionId) {
        return participantRepository.findBySessionId(sessionId);
    }
}
