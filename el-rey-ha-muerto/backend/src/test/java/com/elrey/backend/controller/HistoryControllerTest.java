package com.elrey.backend.controller;

import com.elrey.backend.dto.*;
import com.elrey.backend.service.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = HistoryController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class HistoryControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean HistoryService historyService;

    @Test
    void GET_historial_devuelve_lista_de_sesiones() throws Exception {
        List<SessionSummaryDto> sessions = List.of(
                new SessionSummaryDto(1L, "Rufo", "2026-06-01T10:00", "2026-06-01T10:05", 3, false, "Hambre"),
                new SessionSummaryDto(2L, "Carlos", "2026-06-02T10:00", "2026-06-02T10:10", 10, true, null)
        );
        when(historyService.getAllSessions()).thenReturn(sessions);

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].playerName").value("Rufo"))
                .andExpect(jsonPath("$[0].isAlive").value(false))
                .andExpect(jsonPath("$[0].causeOfDeath").value("Hambre"))
                .andExpect(jsonPath("$[1].playerName").value("Carlos"))
                .andExpect(jsonPath("$[1].isAlive").value(true));
    }

    @Test
    void GET_historial_devuelve_array_vacio_si_no_hay_sesiones() throws Exception {
        when(historyService.getAllSessions()).thenReturn(List.of());

        mockMvc.perform(get("/api/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void GET_detalle_devuelve_sesion_con_log_completo() throws Exception {
        List<SessionChoiceLogDto> log = List.of(
                new SessionChoiceLogDto(1, "Evento día 1", "Opción A", false, null),
                new SessionChoiceLogDto(2, "Fin del Reinado", "—", false, "Murió de hambre.")
        );
        SessionDetailDto detail = new SessionDetailDto(1L, "Rufo", 2, false, "Murió de hambre.", log);
        when(historyService.getSessionDetail(1L)).thenReturn(detail);

        mockMvc.perform(get("/api/history/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Rufo"))
                .andExpect(jsonPath("$.daysSurvived").value(2))
                .andExpect(jsonPath("$.isAlive").value(false))
                .andExpect(jsonPath("$.log.length()").value(2))
                .andExpect(jsonPath("$.log[0].eventTitle").value("Evento día 1"))
                .andExpect(jsonPath("$.log[1].eventTitle").value("Fin del Reinado"))
                .andExpect(jsonPath("$.log[1].deathMessage").value("Murió de hambre."));
    }

    @Test
    void GET_detalle_sesion_victoria_sin_entrada_muerte() throws Exception {
        List<SessionChoiceLogDto> log = List.of(
                new SessionChoiceLogDto(5, "Evento día 5", "Opción B", false, null)
        );
        SessionDetailDto detail = new SessionDetailDto(2L, "Carlos", 10, true, null, log);
        when(historyService.getSessionDetail(2L)).thenReturn(detail);

        mockMvc.perform(get("/api/history/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAlive").value(true))
                .andExpect(jsonPath("$.causeOfDeath").doesNotExist())
                .andExpect(jsonPath("$.log.length()").value(1));
    }
}
