package com.elrey.backend.service;

import com.elrey.backend.dto.*;
import com.elrey.backend.entity.*;
import com.elrey.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GameService {

    private final GameSessionRepository sessionRepo;
    private final KingStatsRepository statsRepo;
    private final GameEventRepository eventRepo;
    private final ChoiceRepository choiceRepo;
    private final SessionChoiceRepository sessionChoiceRepo;
    private final ActiveFlagRepository activeFlagRepo;

    // ── Iniciar partida ───────────────────────────────────────────────────────

    public GameStateDto startGame(String playerName) {
        GameSession session = GameSession.builder()
                .playerName(playerName)
                .startedAt(LocalDateTime.now())
                .isAlive(true)
                .daysSurvived(1)
                .build();
        session = sessionRepo.save(session);

        KingStats stats = KingStats.builder()
                .sessionId(session.getId())
                .day(1)
                .hygiene(100).hunger(100).popularity(50).wealth(100)
                .build();
        stats = statsRepo.save(stats);

        GameEvent event = eventRepo.findFirstByDayTarget(1)
                .orElseThrow(() -> new IllegalStateException("No hay evento para el día 1"));

        return buildState(session, stats, event,
                "El primer día de tu glorioso reinado ha comenzado. ¡Que los dioses te acompañen!");
    }

    // ── Recuperar estado actual ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public GameStateDto getState(Long sessionId) {
        GameSession session = findSession(sessionId);
        KingStats stats = findStats(sessionId);

        if (!Boolean.TRUE.equals(session.getIsAlive())) {
            return buildState(session, stats, null, session.getCauseOfDeath());
        }

        GameEvent event = eventRepo.findFirstByDayTarget(stats.getDay())
                .orElseThrow(() -> new IllegalStateException("No hay evento para el día " + stats.getDay()));
        return buildState(session, stats, event, narratorFor(stats.getDay()));
    }

    // ── Procesar una decisión ─────────────────────────────────────────────────

    public GameStateDto processChoice(Long sessionId, Long choiceId) {
        GameSession session = findSession(sessionId);
        if (!Boolean.TRUE.equals(session.getIsAlive())) {
            throw new IllegalStateException("Esta partida ya ha terminado");
        }

        KingStats current = findStats(sessionId);
        Choice choice = choiceRepo.findById(choiceId)
                .orElseThrow(() -> new IllegalArgumentException("Decisión no encontrada: " + choiceId));

        // Registrar la elección
        sessionChoiceRepo.save(SessionChoice.builder()
                .session(session)
                .event(choice.getEvent())
                .choice(choice)
                .day(current.getDay())
                .chosenAt(LocalDateTime.now())
                .build());

        // Muerte inmediata
        if (Boolean.TRUE.equals(choice.getImmediateDeath())) {
            session.setDaysSurvived(current.getDay());
            return endGame(session, current, choice.getDeathMessage());
        }

        // Aplicar cambios de stats (límites 0–100)
        int hygiene    = clamp(current.getHygiene()    + choice.getStatHygiene());
        int hunger     = clamp(current.getHunger()     + choice.getStatHunger());
        int popularity = clamp(current.getPopularity() + choice.getStatPopularity());
        int wealth     = clamp(current.getWealth()     + choice.getStatWealth());

        // Activar flag oculta si la hay
        if (choice.getHiddenFlag() != null) {
            activeFlagRepo.save(ActiveFlag.builder()
                    .sessionId(sessionId)
                    .flagName(choice.getHiddenFlag())
                    .triggerDay(current.getDay() + choice.getFlagTriggerDelay())
                    .deathMessage(choice.getDeathMessage())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        // Comprobar muerte por stats
        String statDeath = checkStatDeath(hygiene, hunger, popularity, wealth);
        if (statDeath != null) {
            KingStats dead = saveStats(sessionId, current.getDay(), hygiene, hunger, popularity, wealth);
            session.setDaysSurvived(current.getDay());
            return endGame(session, dead, statDeath);
        }

        int nextDay = current.getDay() + 1;

        // Comprobar flags que disparan en el día siguiente
        List<ActiveFlag> triggered = activeFlagRepo.findBySessionIdAndTriggerDay(sessionId, nextDay);
        if (!triggered.isEmpty()) {
            KingStats next = saveStats(sessionId, nextDay, hygiene, hunger, popularity, wealth);
            session.setDaysSurvived(nextDay);
            return endGame(session, next, triggered.get(0).getDeathMessage());
        }

        // ¡Supervivencia! (se superan los 10 días)
        if (nextDay > 10) {
            KingStats final_ = saveStats(sessionId, 10, hygiene, hunger, popularity, wealth);
            session.setIsAlive(true);
            session.setDaysSurvived(10);
            session.setEndedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildState(session, final_, null,
                    "¡Increíble! Has sobrevivido los 10 días. El reino está... sorprendentemente intacto.");
        }

        // Avanzar al día siguiente
        KingStats next = saveStats(sessionId, nextDay, hygiene, hunger, popularity, wealth);
        session.setDaysSurvived(nextDay);
        sessionRepo.save(session);

        GameEvent nextEvent = eventRepo.findFirstByDayTarget(nextDay)
                .orElseThrow(() -> new IllegalStateException("No hay evento para el día " + nextDay));

        return buildState(session, next, nextEvent, narratorFor(nextDay));
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private GameStateDto endGame(GameSession session, KingStats stats, String deathMsg) {
        session.setIsAlive(false);
        session.setEndedAt(LocalDateTime.now());
        session.setCauseOfDeath(deathMsg);
        sessionRepo.save(session);
        return buildState(session, stats, null, deathMsg);
    }

    private GameStateDto buildState(GameSession session, KingStats stats,
                                    GameEvent event, String narrator) {
        KingStatsDto statsDto = new KingStatsDto(
                stats.getHygiene(), stats.getHunger(),
                stats.getPopularity(), stats.getWealth());

        GameEventDto eventDto = null;
        if (event != null) {
            List<ChoiceDto> choices = event.getChoices().stream()
                    .map(c -> new ChoiceDto(c.getId(), c.getLabel(), c.getText()))
                    .toList();
            eventDto = new GameEventDto(
                    event.getId(), event.getTitle(),
                    event.getDescription(), event.getScene(), choices);
        }

        return new GameStateDto(
                session.getId(), session.getPlayerName(),
                stats.getDay(), Boolean.TRUE.equals(session.getIsAlive()),
                statsDto, eventDto, narrator, session.getCauseOfDeath());
    }

    private KingStats saveStats(Long sessionId, int day,
                                int hygiene, int hunger, int popularity, int wealth) {
        return statsRepo.save(KingStats.builder()
                .sessionId(sessionId).day(day)
                .hygiene(hygiene).hunger(hunger)
                .popularity(popularity).wealth(wealth)
                .build());
    }

    private GameSession findSession(Long id) {
        return sessionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sesión no encontrada: " + id));
    }

    private KingStats findStats(Long sessionId) {
        return statsRepo.findTopBySessionIdOrderByDayDesc(sessionId)
                .orElseThrow(() -> new IllegalStateException("Stats no encontradas para sesión " + sessionId));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String checkStatDeath(int hygiene, int hunger, int popularity, int wealth) {
        if (hygiene    <= 0) return "Falleciste de una enfermedad causada por higiene catastrófica. El reino te recuerda... con cierto asco.";
        if (hunger     <= 0) return "El hambre se cobró su precio. Un rey que no come no reina por mucho tiempo.";
        if (popularity <= 0) return "El pueblo se rebeló. Tu popularidad llegó a cero y contigo llegó también tu fin.";
        if (wealth     <= 0) return "La bancarrota real es mortal. Sin dinero, hasta los guardias dejaron de protegerte.";
        return null;
    }

    private String narratorFor(int day) {
        return switch (day) {
            case 1  -> "El primer día de tu glorioso reinado ha comenzado.";
            case 2  -> "Segundo día. Las intrigas del castillo empiezan a manifestarse.";
            case 3  -> "Tercer día. El reino observa cada uno de tus movimientos.";
            case 4  -> "Cuarto día. Algo en el ambiente huele raro... y no es solo tú.";
            case 5  -> "Quinto día. La mitad del reinado. ¿Seguirás vivo mañana?";
            case 6  -> "Sexto día. Los rumores del castillo llegan a tus oídos.";
            case 7  -> "Séptimo día. Una semana reinando. Casi un récord para tu dinastía.";
            case 8  -> "Octavo día. El final se acerca. ¿Será tuyo o del reino?";
            case 9  -> "Noveno día. Un paso más y lo habrás conseguido.";
            case 10 -> "Décimo día. El último. Que los dioses te protejan.";
            default -> "El día continúa...";
        };
    }
}
