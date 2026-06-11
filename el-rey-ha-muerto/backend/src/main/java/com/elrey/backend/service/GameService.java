package com.elrey.backend.service;

import com.elrey.backend.dto.*;
import com.elrey.backend.entity.*;
import com.elrey.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final SessionEventPlaylistRepository playlistRepo;

    // ── Iniciar partida ───────────────────────────────────────────────────────

    public GameStateDto startGame(String playerName) {
        GameSession session = GameSession.builder()
                .playerName(playerName)
                .startedAt(LocalDateTime.now())
                .isAlive(true)
                .daysSurvived(1)
                .build();
        session = sessionRepo.save(session);

        buildAndSavePlaylist(session.getId());

        KingStats stats = KingStats.builder()
                .sessionId(session.getId())
                .day(1)
                .hygiene(100).hunger(100).popularity(50).wealth(100)
                .build();
        stats = statsRepo.save(stats);

        GameEvent event = findEventForPosition(session.getId(), 1);

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

        // Victoria: partida terminada con el jugador vivo
        if (session.getEndedAt() != null) {
            return buildState(session, stats, null,
                    "¡Increíble! Has sobrevivido todos los días. El reino está... sorprendentemente intacto.");
        }

        GameEvent event = findEventForPosition(sessionId, stats.getDay());
        return buildState(session, stats, event, narratorFor(stats.getDay()));
    }

    // ── Procesar una decisión ─────────────────────────────────────────────────

    public GameStateDto processChoice(Long sessionId, Long choiceId) {
        GameSession session = findSession(sessionId);
        if (!Boolean.TRUE.equals(session.getIsAlive()) || session.getEndedAt() != null) {
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

        // ¿Hay más eventos en la playlist? Si no, el rey ha ganado
        boolean hasNextEvent = playlistRepo.findBySessionIdAndPosition(sessionId, nextDay).isPresent();
        if (!hasNextEvent) {
            KingStats final_ = saveStats(sessionId, current.getDay(), hygiene, hunger, popularity, wealth);
            session.setIsAlive(true);
            session.setDaysSurvived(current.getDay());
            session.setEndedAt(LocalDateTime.now());
            sessionRepo.save(session);
            return buildState(session, final_, null,
                    "¡Increíble! Has sobrevivido todos los días. El reino está... sorprendentemente intacto.");
        }

        // Avanzar al día siguiente
        KingStats next = saveStats(sessionId, nextDay, hygiene, hunger, popularity, wealth);
        session.setDaysSurvived(nextDay);
        sessionRepo.save(session);

        return buildState(session, next, findEventForPosition(sessionId, nextDay), narratorFor(nextDay));
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    // Máximo de eventos intermedios (posiciones 2-19) para que el juego sea siempre 20 días
    private static final int MAX_MIDDLE_EVENTS = 18;

    private void buildAndSavePlaylist(Long sessionId) {
        GameEvent first = eventRepo.findFirstByDayTargetAndSource(1, "manual")
                .orElseThrow(() -> new IllegalStateException("No hay evento inicial manual (día 1)"));
        GameEvent last = eventRepo.findFirstByDayTargetAndSource(10, "manual")
                .orElseThrow(() -> new IllegalStateException("No hay evento final manual (día 10)"));

        List<GameEvent> all = eventRepo.findAll();
        List<GameEvent> pool = new ArrayList<>(all.stream()
                .filter(e -> !e.getId().equals(first.getId()) && !e.getId().equals(last.getId()))
                .toList());
        Collections.shuffle(pool);

        // Limitar a MAX_MIDDLE_EVENTS para mantener siempre 20 días de juego
        List<GameEvent> middle = pool.subList(0, Math.min(MAX_MIDDLE_EVENTS, pool.size()));

        List<Long> ids = new ArrayList<>();
        ids.add(first.getId());
        middle.forEach(e -> ids.add(e.getId()));
        ids.add(last.getId());

        for (int i = 0; i < ids.size(); i++) {
            playlistRepo.save(SessionEventPlaylist.builder()
                    .sessionId(sessionId)
                    .position(i + 1)
                    .eventId(ids.get(i))
                    .build());
        }
    }

    private GameEvent findEventForPosition(Long sessionId, int position) {
        SessionEventPlaylist entry = playlistRepo.findBySessionIdAndPosition(sessionId, position)
                .orElseThrow(() -> new IllegalStateException("No hay evento en posición " + position));
        return eventRepo.findById(entry.getEventId())
                .orElseThrow(() -> new IllegalStateException("Evento no encontrado: " + entry.getEventId()));
    }

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
            case 5  -> "Quinto día. El reino no deja de sorprenderte.";
            case 6  -> "Sexto día. Los rumores del castillo llegan a tus oídos.";
            case 7  -> "Séptimo día. Una semana reinando. Casi un récord para tu dinastía.";
            case 8  -> "Octavo día. Las tensiones del reino van en aumento.";
            case 9  -> "Noveno día. Algo se trama en las sombras del castillo.";
            case 10 -> "Décimo día. Has sobrevivido más que la mayoría. El reino empieza a respetarte.";
            case 11 -> "Undécimo día. Las noticias del reino llegan hasta tus aposentos.";
            case 12 -> "Duodécimo día. El peso de la corona se hace notar.";
            case 13 -> "Decimotercer día. Trece días de reinado. Los supersticiosos se persignan.";
            case 14 -> "Decimocuarto día. Dos semanas en el trono. Eso ya es historia.";
            case 15 -> "Decimoquinto día. La mitad del camino. ¿Llegarás al final?";
            case 16 -> "Decimosexto día. El reino empieza a susurrar sobre tu longevidad.";
            case 17 -> "Decimoséptimo día. Solo tres días más separan al mediocre del legendario.";
            case 18 -> "Decimoctavo día. Los nobles empiezan a respetarte de verdad.";
            case 19 -> "Decimonoveno día. Un último escollo antes de la gloria eterna.";
            case 20 -> "Vigésimo día. El último. Que los dioses te protejan.";
            default -> "El día continúa...";
        };
    }
}
