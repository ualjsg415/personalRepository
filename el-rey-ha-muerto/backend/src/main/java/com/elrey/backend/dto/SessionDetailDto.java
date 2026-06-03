package com.elrey.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record SessionDetailDto(
        Long id,
        String playerName,
        int daysSurvived,
        @JsonProperty("isAlive") boolean isAlive,
        String causeOfDeath,
        List<SessionChoiceLogDto> log
) {}
