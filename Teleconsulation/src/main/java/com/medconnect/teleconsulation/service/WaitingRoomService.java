package com.medconnect.teleconsulation.service;

import com.medconnect.teleconsulation.dto.response.WaitingRoomResponse;
import com.medconnect.teleconsulation.exception.SessionNotFoundException;
import com.medconnect.teleconsulation.model.SessionStatus;
import com.medconnect.teleconsulation.model.VideoSession;
import com.medconnect.teleconsulation.model.WaitingRoom;
import com.medconnect.teleconsulation.repository.VideoSessionRepository;
import com.medconnect.teleconsulation.repository.WaitingRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WaitingRoomService {

    private static final int AVG_CONSULTATION_MINUTES = 15;

    private final WaitingRoomRepository waitingRoomRepository;
    private final VideoSessionRepository videoSessionRepository;

    public WaitingRoomResponse addToWaitingRoom(String sessionId, String patientId) {
        VideoSession session = getSession(sessionId);

        if (session.getStatus() == SessionStatus.ENDED
                || session.getStatus() == SessionStatus.FORCE_ENDED) {
            throw new IllegalStateException(
                    "Cannot join waiting room: session " + sessionId + " has already ended");
        }

        waitingRoomRepository.findBySessionIdAndPatientId(sessionId, patientId)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Patient " + patientId + " is already in the waiting room for session: " + sessionId);
                });

        long nextPosition = waitingRoomRepository.countBySessionIdAndAdmittedFalse(sessionId) + 1;

        WaitingRoom entry = WaitingRoom.builder()
                .sessionId(sessionId)
                .doctorId(session.getDoctorId())
                .patientId(patientId)
                .position((int) nextPosition)
                .joinedAt(LocalDateTime.now())
                .admitted(false)
                .build();

        entry = waitingRoomRepository.save(entry);

        return WaitingRoomResponse.builder()
                .sessionId(sessionId)
                .patientId(patientId)
                .position(entry.getPosition())
                .estimatedWaitMinutes((int) ((nextPosition - 1) * AVG_CONSULTATION_MINUTES))
                .joinedAt(entry.getJoinedAt())
                .admitted(false)
                .build();
    }

    public WaitingRoomResponse getQueuePosition(String patientId) {
        WaitingRoom entry = waitingRoomRepository.findByPatientIdAndAdmittedFalse(patientId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Patient " + patientId + " is not in any waiting room"));
        int estimatedWait = (entry.getPosition() - 1) * AVG_CONSULTATION_MINUTES;
        return WaitingRoomResponse.builder()
                .sessionId(entry.getSessionId())
                .patientId(patientId)
                .position(entry.getPosition())
                .estimatedWaitMinutes(estimatedWait)
                .joinedAt(entry.getJoinedAt())
                .admitted(entry.isAdmitted())
                .build();
    }

    public WaitingRoomResponse admitFromWaitingRoom(String sessionId, String patientId) {
        WaitingRoom entry;
        if (patientId != null && !patientId.isBlank()) {
            entry = waitingRoomRepository.findBySessionIdAndPatientId(sessionId, patientId)
                    .orElseThrow(() -> new SessionNotFoundException(
                            "Patient " + patientId + " not found in waiting room for session: " + sessionId));
        } else {
            List<WaitingRoom> queue = waitingRoomRepository
                    .findBySessionIdAndAdmittedFalseOrderByPositionAsc(sessionId);
            if (queue.isEmpty()) {
                throw new IllegalStateException("No patients waiting for session: " + sessionId);
            }
            entry = queue.get(0);
        }

        entry.setAdmitted(true);
        entry.setAdmittedAt(LocalDateTime.now());
        entry = waitingRoomRepository.save(entry);

        return WaitingRoomResponse.builder()
                .sessionId(sessionId)
                .patientId(entry.getPatientId())
                .position(entry.getPosition())
                .estimatedWaitMinutes(0)
                .joinedAt(entry.getJoinedAt())
                .admitted(true)
                .build();
    }

    public int estimateWaitTime(String sessionId) {
        long waiting = waitingRoomRepository.countBySessionIdAndAdmittedFalse(sessionId);
        return (int) (waiting * AVG_CONSULTATION_MINUTES);
    }

    public WaitingRoomResponse getWaitQueue(String doctorId) {
        List<WaitingRoom> queue = waitingRoomRepository
                .findByDoctorIdAndAdmittedFalseOrderByPositionAsc(doctorId);

        List<WaitingRoomResponse.QueueEntry> entries = queue.stream()
                .map(w -> WaitingRoomResponse.QueueEntry.builder()
                        .patientId(w.getPatientId())
                        .position(w.getPosition())
                        .joinedAt(w.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return WaitingRoomResponse.builder()
                .sessionId(null)
                .estimatedWaitMinutes(queue.size() * AVG_CONSULTATION_MINUTES)
                .queue(entries)
                .build();
    }

    private VideoSession getSession(String sessionId) {
        return videoSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found: " + sessionId));
    }
}
