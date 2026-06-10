package com.elrey.backend.service;

import com.elrey.backend.dto.SessionDetailDto;
import com.elrey.backend.dto.SessionSummaryDto;
import com.elrey.backend.entity.*;
import com.elrey.backend.repository.GameSessionRepository;
import com.elrey.backend.repository.SessionChoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock GameSessionRepository sessionRepo;
    @Mock SessionChoiceRepository sessionChoiceRepo;

    @InjectMocks HistoryService historyService;

    @Test
    void getAllSessions_devuelve_sesiones_terminadas() {
        GameSession s1 = GameSession.builder().id(1L).playerName("Rufo")
                .daysSurvived(3).isAlive(false).causeOfDeath("Hambre")
                .startedAt(LocalDateTime.now()).endedAt(LocalDateTime.now()).build();
        GameSession s2 = GameSession.builder().id(2L).playerName("Carlos")
                .daysSurvived(10).isAlive(true)
                .startedAt(LocalDateTime.now()).endedAt(LocalDateTime.now()).build();

        when(sessionRepo.findAllByEndedAtIsNotNullOrderByStartedAtAsc()).thenReturn(List.of(s1, s2));

        List<SessionSummaryDto> result = historyService.getAllSessions();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).playerName()).isEqualTo("Rufo");
        assertThat(result.get(0).isAlive()).isFalse();
        assertThat(result.get(0).causeOfDeath()).isEqualTo("Hambre");
        assertThat(result.get(1).playerName()).isEqualTo("Carlos");
        assertThat(result.get(1).isAlive()).isTrue();
    }

    @Test
    void getAllSessions_devuelve_lista_vacia_si_no_hay_sesiones() {
        when(sessionRepo.findAllByEndedAtIsNotNullOrderByStartedAtAsc()).thenReturn(List.of());

        assertThat(historyService.getAllSessions()).isEmpty();
    }

    @Test
    void getSessionDetail_incluye_log_de_decisiones_y_entrada_de_muerte() {
        GameSession session = GameSession.builder().id(1L).playerName("Rufo")
                .daysSurvived(2).isAlive(false).causeOfDeath("Falleciste por hambre.")
                .build();

        GameEvent event = GameEvent.builder().id(1L).title("Evento día 1").build();
        Choice choice = Choice.builder().id(1L).text("Ayunar").hiddenFlag(null).build();
        SessionChoice sc = SessionChoice.builder()
                .id(1L).session(session).event(event).choice(choice).day(1).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(sessionChoiceRepo.findBySessionIdOrderByDayAsc(1L)).thenReturn(List.of(sc));

        SessionDetailDto result = historyService.getSessionDetail(1L);

        assertThat(result.playerName()).isEqualTo("Rufo");
        assertThat(result.daysSurvived()).isEqualTo(2);
        assertThat(result.isAlive()).isFalse();
        assertThat(result.log()).hasSize(2);
        assertThat(result.log().get(0).eventTitle()).isEqualTo("Evento día 1");
        assertThat(result.log().get(0).choiceText()).isEqualTo("Ayunar");
        assertThat(result.log().get(1).eventTitle()).isEqualTo("Fin del Reinado");
        assertThat(result.log().get(1).deathMessage()).isEqualTo("Falleciste por hambre.");
    }

    @Test
    void getSessionDetail_no_añade_entrada_muerte_si_rey_sobrevivio() {
        GameSession session = GameSession.builder().id(2L).playerName("Carlos")
                .daysSurvived(10).isAlive(true).causeOfDeath(null).build();

        when(sessionRepo.findById(2L)).thenReturn(Optional.of(session));
        when(sessionChoiceRepo.findBySessionIdOrderByDayAsc(2L)).thenReturn(List.of());

        SessionDetailDto result = historyService.getSessionDetail(2L);

        assertThat(result.isAlive()).isTrue();
        assertThat(result.causeOfDeath()).isNull();
        assertThat(result.log()).isEmpty();
    }

    @Test
    void getSessionDetail_lanza_excepcion_si_sesion_no_existe() {
        when(sessionRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historyService.getSessionDetail(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sesión no encontrada");
    }

    @Test
    void getSessionDetail_marca_hasHiddenFlag_cuando_choice_tiene_flag() {
        GameSession session = GameSession.builder().id(1L).playerName("Test")
                .daysSurvived(1).isAlive(false).causeOfDeath("Muerte por flag.").build();
        GameEvent event = GameEvent.builder().id(1L).title("Evento").build();
        Choice choice = Choice.builder().id(1L).text("Decisión peligrosa")
                .hiddenFlag("infeccion_bucal").build();
        SessionChoice sc = SessionChoice.builder()
                .id(1L).session(session).event(event).choice(choice).day(1).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(sessionChoiceRepo.findBySessionIdOrderByDayAsc(1L)).thenReturn(List.of(sc));

        SessionDetailDto result = historyService.getSessionDetail(1L);

        assertThat(result.log().get(0).hasHiddenFlag()).isTrue();
    }

    @Test
    void getSessionDetail_hasHiddenFlag_false_cuando_choice_no_tiene_flag() {
        GameSession session = GameSession.builder().id(1L).playerName("Test")
                .daysSurvived(3).isAlive(true).build();
        GameEvent event = GameEvent.builder().id(1L).title("Evento").build();
        Choice choice = Choice.builder().id(1L).text("Opción segura").hiddenFlag(null).build();
        SessionChoice sc = SessionChoice.builder()
                .id(1L).session(session).event(event).choice(choice).day(1).build();

        when(sessionRepo.findById(1L)).thenReturn(Optional.of(session));
        when(sessionChoiceRepo.findBySessionIdOrderByDayAsc(1L)).thenReturn(List.of(sc));

        SessionDetailDto result = historyService.getSessionDetail(1L);

        assertThat(result.log().get(0).hasHiddenFlag()).isFalse();
    }
}
