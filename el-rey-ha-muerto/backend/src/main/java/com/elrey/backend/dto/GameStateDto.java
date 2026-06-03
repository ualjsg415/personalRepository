package com.elrey.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GameStateDto(
        Long sessionId,
        String playerName,
        int day,
        @JsonProperty("isAlive") boolean isAlive,
        KingStatsDto stats,
        GameEventDto currentEvent,
        String narratorMessage,
        String causeOfDeath
) {}
