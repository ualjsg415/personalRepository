package com.elrey.backend.service;

import com.elrey.backend.dto.*;
import com.elrey.backend.entity.GameSession;
import com.elrey.backend.entity.SessionChoice;
import com.elrey.backend.repository.GameSessionRepository;
import com.elrey.backend.repository.SessionChoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HistoryService {

    private final GameSessionRepository sessionRepo;
    private final SessionChoiceRepository sessionChoiceRepo;

    public List<SessionSummaryDto> getAllSessions() {
        return sessionRepo.findAllByEndedAtIsNotNullOrderByStartedAtAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    public SessionDetailDto getSessionDetail(Long id) {
        GameSession session = sessionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + id));

        List<SessionChoice> choices = sessionChoiceRepo.findBySessionIdOrderByDayAsc(id);

        List<SessionChoiceLogDto> log = new ArrayList<>();
        for (SessionChoice sc : choices) {
            log.add(new SessionChoiceLogDto(
                    sc.getDay(),
                    sc.getEvent().getTitle(),
                    sc.getChoice().getText(),
                    sc.getChoice().getHiddenFlag() != null,
                    null
            ));
        }

        // Entrada final de muerte si la partida terminó
        if (!Boolean.TRUE.equals(session.getIsAlive()) && session.getCauseOfDeath() != null) {
            log.add(new SessionChoiceLogDto(
                    session.getDaysSurvived(),
                    "Fin del Reinado",
                    "—",
                    false,
                    session.getCauseOfDeath()
            ));
        }

        return new SessionDetailDto(
                session.getId(),
                session.getPlayerName(),
                session.getDaysSurvived(),
                Boolean.TRUE.equals(session.getIsAlive()),
                session.getCauseOfDeath(),
                log
        );
    }

    private SessionSummaryDto toSummary(GameSession s) {
        return new SessionSummaryDto(
                s.getId(),
                s.getPlayerName(),
                s.getStartedAt() != null ? s.getStartedAt().toString() : null,
                s.getEndedAt()   != null ? s.getEndedAt().toString()   : null,
                s.getDaysSurvived(),
                Boolean.TRUE.equals(s.getIsAlive()),
                s.getCauseOfDeath()
        );
    }
}
