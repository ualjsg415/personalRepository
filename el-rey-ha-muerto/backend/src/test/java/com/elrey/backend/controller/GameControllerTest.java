package com.elrey.backend.controller;

import com.elrey.backend.dto.*;
import com.elrey.backend.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = GameController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
class GameControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean GameService gameService;

    private GameStateDto estadoVivo() {
        KingStatsDto stats = new KingStatsDto(100, 100, 50, 100);
        GameEventDto event = new GameEventDto(1L, "Evento Test", "Descripción", "throne-room",
                List.of(new ChoiceDto(1L, "A", "Opción A"), new ChoiceDto(2L, "B", "Opción B")));
        return new GameStateDto(1L, "Rufo", 1, true, stats, event,
                "El primer día de tu glorioso reinado ha comenzado.", null);
    }

    @Test
    void POST_start_devuelve_estado_inicial_con_stats_y_evento() throws Exception {
        when(gameService.startGame("Rufo")).thenReturn(estadoVivo());

        mockMvc.perform(post("/api/game/start")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new StartGameRequest("Rufo"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerName").value("Rufo"))
                .andExpect(jsonPath("$.day").value(1))
                .andExpect(jsonPath("$.isAlive").value(true))
                .andExpect(jsonPath("$.stats.hygiene").value(100))
                .andExpect(jsonPath("$.stats.popularity").value(50))
                .andExpect(jsonPath("$.currentEvent.title").value("Evento Test"))
                .andExpect(jsonPath("$.currentEvent.choices.length()").value(2));
    }

    @Test
    void GET_state_devuelve_estado_sesion_viva() throws Exception {
        when(gameService.getState(1L)).thenReturn(estadoVivo());

        mockMvc.perform(get("/api/game/1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(1))
                .andExpect(jsonPath("$.isAlive").value(true))
                .andExpect(jsonPath("$.currentEvent").isNotEmpty());
    }

    @Test
    void GET_state_devuelve_estado_rey_muerto() throws Exception {
        GameStateDto dead = new GameStateDto(1L, "Rufo", 3, false,
                new KingStatsDto(0, 80, 50, 80), null, null,
                "Falleciste de una enfermedad causada por higiene catastrófica.");

        when(gameService.getState(1L)).thenReturn(dead);

        mockMvc.perform(get("/api/game/1/state"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAlive").value(false))
                .andExpect(jsonPath("$.currentEvent").doesNotExist())
                .andExpect(jsonPath("$.causeOfDeath").value("Falleciste de una enfermedad causada por higiene catastrófica."));
    }

    @Test
    void POST_choose_avanza_al_dia_siguiente() throws Exception {
        GameStateDto day2 = new GameStateDto(1L, "Rufo", 2, true,
                new KingStatsDto(95, 100, 55, 100),
                new GameEventDto(2L, "Evento día 2", "Desc", "bedroom", List.of()),
                "Segundo día.", null);

        when(gameService.processChoice(1L, 3L)).thenReturn(day2);

        mockMvc.perform(post("/api/game/1/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChoiceRequest(3L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.day").value(2))
                .andExpect(jsonPath("$.isAlive").value(true));
    }

    @Test
    void POST_choose_devuelve_estado_muerte() throws Exception {
        GameStateDto dead = new GameStateDto(1L, "Rufo", 2, false,
                new KingStatsDto(80, 0, 50, 80), null, null,
                "El hambre se cobró su precio.");

        when(gameService.processChoice(1L, 5L)).thenReturn(dead);

        mockMvc.perform(post("/api/game/1/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChoiceRequest(5L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAlive").value(false))
                .andExpect(jsonPath("$.causeOfDeath").value("El hambre se cobró su precio."));
    }

    @Test
    void POST_choose_devuelve_victoria() throws Exception {
        GameStateDto victory = new GameStateDto(1L, "Rufo", 10, true,
                new KingStatsDto(70, 75, 60, 85), null,
                "¡Increíble! Has sobrevivido los 10 días.", null);

        when(gameService.processChoice(1L, 7L)).thenReturn(victory);

        mockMvc.perform(post("/api/game/1/choose")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ChoiceRequest(7L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAlive").value(true))
                .andExpect(jsonPath("$.currentEvent").doesNotExist())
                .andExpect(jsonPath("$.narratorMessage").value("¡Increíble! Has sobrevivido los 10 días."));
    }
}
