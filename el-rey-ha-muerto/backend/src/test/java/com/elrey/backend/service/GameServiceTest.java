package com.elrey.backend.service;

import com.elrey.backend.dto.*;
import com.elrey.backend.entity.*;
import com.elrey.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock GameSessionRepository sessionRepo;
    @Mock KingStatsRepository statsRepo;
    @Mock GameEventRepository eventRepo;
    @Mock ChoiceRepository choiceRepo;
    @Mock SessionChoiceRepository sessionChoiceRepo;
    @Mock ActiveFlagRepository activeFlagRepo;

    @InjectMocks GameService gameService;

    private GameSession aliveSession;
    private KingStats currentStats;
    private GameEvent testEvent;
    private Choice normalChoice;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gameService, "aiEventsEnabled", true);

        aliveSession = GameSession.builder()
                .id(1L).playerName("Rufo").isAlive(true).daysSurvived(1)
                .startedAt(LocalDateTime.now()).build();

        currentStats = KingStats.builder()
                .id(1L).sessionId(1L).day(1)
                .hygiene(100).hunger(100).popularity(50).wealth(100)
                .build();

        normalChoice = Choice.builder()
                .id(10L).label("A").text("Opción A")
                .statHygiene(-5).statHunger(0).statPopularity(5).statWealth(0)
                .immediateDeath(false)
                .build();

        testEvent = GameEvent.builder()
                .id(1L).dayTarget(1).title("Evento día 1")
                .description("Descripción").scene("throne-room")
                .choices(List.of(normalChoice))
                .build();
        normalChoice.setEvent(testEvent);
    }

    // ── startGame ──────────────────────────────────────────────────────────────

    @Test
    void startGame_crea_sesion_con_stats_iniciales() {
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(1, "ai_generated"))
                .thenReturn(Optional.of(testEvent));

        GameStateDto result = gameService.startGame("Rufo");

        assertThat(result.playerName()).isEqualTo("Rufo");
        assertThat(result.day()).isEqualTo(1);
        assertThat(result.isAlive()).isTrue();
        assertThat(result.stats().hygiene()).isEqualTo(100);
        assertThat(result.stats().hunger()).isEqualTo(100);
        assertThat(result.stats().popularity()).isEqualTo(50);
        assertThat(result.stats().wealth()).isEqualTo(100);
        assertThat(result.currentEvent().title()).isEqualTo("Evento día 1");
    }

    @Test
    void startGame_usa_evento_manual_si_ai_desactivada() {
        ReflectionTestUtils.setField(gameService, "aiEventsEnabled", false);
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(eventRepo.findFirstByDayTarget(1)).thenReturn(Optional.of(testEvent));

        GameStateDto result = gameService.startGame("Rufo");

        assertThat(result.currentEvent()).isNotNull();
        verify(eventRepo, never()).findFirstByDayTargetAndSourceOrderByScrapedAtDesc(anyInt(), anyString());
    }

    @Test
    void startGame_cae_a_manual_si_no_hay_ai_generated() {
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(1, "ai_generated"))
                .thenReturn(Optional.empty());
        when(eventRepo.findFirstByDayTarget(1)).thenReturn(Optional.of(testEvent));

        GameStateDto result = gameService.startGame("Rufo");

        assertThat(result.currentEvent()).isNotNull();
    }

    // ── getState ───────────────────────────────────────────────────────────────

    @Test
    void getState_devuelve_estado_de_sesion_viva() {
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(1, "ai_generated"))
                .thenReturn(Optional.of(testEvent));

        GameStateDto result = gameService.getState(1L);

        assertThat(result.isAlive()).isTrue();
        assertThat(result.currentEvent()).isNotNull();
    }

    @Test
    void getState_devuelve_estado_muerto_sin_evento() {
        aliveSession.setIsAlive(false);
        aliveSession.setCauseOfDeath("Falleciste de higiene catastrófica.");
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));

        GameStateDto result = gameService.getState(1L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.currentEvent()).isNull();
        assertThat(result.causeOfDeath()).contains("higiene");
    }

    @Test
    void getState_lanza_excepcion_si_sesion_no_existe() {
        when(sessionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.getState(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sesión no encontrada");
    }

    // ── processChoice — muerte inmediata ───────────────────────────────────────

    @Test
    void processChoice_muerte_inmediata() {
        Choice suicidaChoice = Choice.builder()
                .id(20L).label("C").text("Beber el veneno")
                .immediateDeath(true).deathMessage("¿En serio bebiste el veneno?")
                .statHygiene(0).statHunger(0).statPopularity(0).statWealth(0)
                .event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(20L)).thenReturn(Optional.of(suicidaChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(sessionRepo.save(any())).thenReturn(aliveSession);

        GameStateDto result = gameService.processChoice(1L, 20L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).isEqualTo("¿En serio bebiste el veneno?");
        assertThat(result.currentEvent()).isNull();
    }

    // ── processChoice — muerte por cada stat ──────────────────────────────────

    @Test
    void processChoice_muerte_por_higiene() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(3)
                .hygiene(5).hunger(80).popularity(50).wealth(80).build();
        Choice badChoice = Choice.builder().id(30L).label("A").text("No ducharse")
                .statHygiene(-20).statHunger(0).statPopularity(0).statWealth(0)
                .immediateDeath(false).event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(30L)).thenReturn(Optional.of(badChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);

        GameStateDto result = gameService.processChoice(1L, 30L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).contains("higiene");
    }

    @Test
    void processChoice_muerte_por_hambre() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(2)
                .hygiene(80).hunger(5).popularity(50).wealth(80).build();
        Choice badChoice = Choice.builder().id(31L).label("A").text("Ayuno real")
                .statHygiene(0).statHunger(-20).statPopularity(0).statWealth(0)
                .immediateDeath(false).event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(31L)).thenReturn(Optional.of(badChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);

        GameStateDto result = gameService.processChoice(1L, 31L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).contains("hambre");
    }

    @Test
    void processChoice_muerte_por_popularidad() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(2)
                .hygiene(80).hunger(80).popularity(5).wealth(80).build();
        Choice badChoice = Choice.builder().id(32L).label("A").text("Subir impuestos")
                .statHygiene(0).statHunger(0).statPopularity(-20).statWealth(0)
                .immediateDeath(false).event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(32L)).thenReturn(Optional.of(badChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);

        GameStateDto result = gameService.processChoice(1L, 32L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).contains("popularidad");
    }

    @Test
    void processChoice_muerte_por_riqueza() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(2)
                .hygiene(80).hunger(80).popularity(50).wealth(5).build();
        Choice badChoice = Choice.builder().id(33L).label("A").text("Gastar las arcas")
                .statHygiene(0).statHunger(0).statPopularity(0).statWealth(-20)
                .immediateDeath(false).event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(33L)).thenReturn(Optional.of(badChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(currentStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);

        GameStateDto result = gameService.processChoice(1L, 33L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).contains("bancarrota");
    }

    // ── processChoice — muerte por flag ───────────────────────────────────────

    @Test
    void processChoice_muerte_por_flag_en_siguiente_dia() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(3)
                .hygiene(80).hunger(80).popularity(50).wealth(80).build();
        Choice flagChoice = Choice.builder().id(40L).label("A").text("Ignorar la infección")
                .statHygiene(0).statHunger(0).statPopularity(0).statWealth(0)
                .immediateDeath(false)
                .hiddenFlag("infeccion_bucal").flagTriggerDelay(1)
                .deathMessage("La infección bucal cobró tu vida.")
                .event(testEvent).build();

        ActiveFlag flag = ActiveFlag.builder()
                .id(1L).sessionId(1L).flagName("infeccion_bucal")
                .triggerDay(4).deathMessage("La infección bucal cobró tu vida.").build();

        KingStats nextStats = KingStats.builder().id(2L).sessionId(1L).day(4)
                .hygiene(80).hunger(80).popularity(50).wealth(80).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(40L)).thenReturn(Optional.of(flagChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(activeFlagRepo.save(any())).thenReturn(flag);
        when(statsRepo.save(any())).thenReturn(nextStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(activeFlagRepo.findBySessionIdAndTriggerDay(1L, 4)).thenReturn(List.of(flag));

        GameStateDto result = gameService.processChoice(1L, 40L);

        assertThat(result.isAlive()).isFalse();
        assertThat(result.causeOfDeath()).contains("infección");
    }

    // ── processChoice — victoria ───────────────────────────────────────────────

    @Test
    void processChoice_victoria_al_superar_dia_10() {
        currentStats = KingStats.builder().id(1L).sessionId(1L).day(10)
                .hygiene(80).hunger(80).popularity(50).wealth(80).build();
        KingStats finalStats = KingStats.builder().id(2L).sessionId(1L).day(10)
                .hygiene(75).hunger(80).popularity(55).wealth(80).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(10L)).thenReturn(Optional.of(normalChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(finalStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(activeFlagRepo.findBySessionIdAndTriggerDay(anyLong(), anyInt())).thenReturn(List.of());

        GameStateDto result = gameService.processChoice(1L, 10L);

        assertThat(result.isAlive()).isTrue();
        assertThat(result.currentEvent()).isNull();
        assertThat(result.narratorMessage()).contains("Increíble");
    }

    // ── processChoice — avance normal ─────────────────────────────────────────

    @Test
    void processChoice_avanza_al_dia_siguiente() {
        GameEvent day2Event = GameEvent.builder()
                .id(2L).dayTarget(2).title("Evento día 2")
                .description("Algo ocurre").scene("bedroom").choices(List.of()).build();
        KingStats day2Stats = KingStats.builder().id(2L).sessionId(1L).day(2)
                .hygiene(95).hunger(100).popularity(55).wealth(100).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(10L)).thenReturn(Optional.of(normalChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(day2Stats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(activeFlagRepo.findBySessionIdAndTriggerDay(anyLong(), anyInt())).thenReturn(List.of());
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(2, "ai_generated"))
                .thenReturn(Optional.of(day2Event));

        GameStateDto result = gameService.processChoice(1L, 10L);

        assertThat(result.isAlive()).isTrue();
        assertThat(result.day()).isEqualTo(2);
        assertThat(result.currentEvent().title()).isEqualTo("Evento día 2");
    }

    // ── processChoice — sesión ya terminada ───────────────────────────────────

    @Test
    void processChoice_lanza_excepcion_si_sesion_ya_terminada() {
        aliveSession.setIsAlive(false);
        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));

        assertThatThrownBy(() -> gameService.processChoice(1L, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya ha terminado");
    }

    // ── stats clamping ─────────────────────────────────────────────────────────

    @Test
    void processChoice_stats_no_superan_100() {
        Choice boostChoice = Choice.builder()
                .id(50L).label("B").text("Festín real")
                .statHygiene(10).statHunger(15).statPopularity(10).statWealth(10)
                .immediateDeath(false).event(testEvent).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(50L)).thenReturn(Optional.of(boostChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenAnswer(inv -> {
            KingStats s = inv.getArgument(0);
            assertThat(s.getHygiene()).isLessThanOrEqualTo(100);
            assertThat(s.getHunger()).isLessThanOrEqualTo(100);
            assertThat(s.getPopularity()).isLessThanOrEqualTo(100);
            assertThat(s.getWealth()).isLessThanOrEqualTo(100);
            return KingStats.builder().id(2L).sessionId(1L).day(2)
                    .hygiene(100).hunger(100).popularity(60).wealth(100).build();
        });
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(activeFlagRepo.findBySessionIdAndTriggerDay(anyLong(), anyInt())).thenReturn(List.of());
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(2, "ai_generated"))
                .thenReturn(Optional.of(testEvent));

        gameService.processChoice(1L, 50L);
    }

    @Test
    void processChoice_stats_no_bajan_de_0_sin_morir() {
        // hygiene=100, -5 = 95; no es death
        Choice mildChoice = Choice.builder()
                .id(60L).label("B").text("Descuido leve")
                .statHygiene(-5).statHunger(0).statPopularity(0).statWealth(0)
                .immediateDeath(false).event(testEvent).build();

        KingStats nextStats = KingStats.builder().id(2L).sessionId(1L).day(2)
                .hygiene(95).hunger(100).popularity(50).wealth(100).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(aliveSession));
        when(statsRepo.findTopBySessionIdOrderByDayDesc(1L)).thenReturn(Optional.of(currentStats));
        when(choiceRepo.findById(60L)).thenReturn(Optional.of(mildChoice));
        when(sessionChoiceRepo.save(any())).thenReturn(null);
        when(statsRepo.save(any())).thenReturn(nextStats);
        when(sessionRepo.save(any())).thenReturn(aliveSession);
        when(activeFlagRepo.findBySessionIdAndTriggerDay(anyLong(), anyInt())).thenReturn(List.of());
        when(eventRepo.findFirstByDayTargetAndSourceOrderByScrapedAtDesc(2, "ai_generated"))
                .thenReturn(Optional.of(testEvent));

        GameStateDto result = gameService.processChoice(1L, 60L);

        assertThat(result.isAlive()).isTrue();
    }
}
