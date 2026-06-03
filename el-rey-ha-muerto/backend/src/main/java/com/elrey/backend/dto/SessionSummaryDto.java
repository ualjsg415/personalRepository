package com.elrey.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SessionSummaryDto(
        Long id,
        String playerName,
        String startedAt,
        String endedAt,
        int daysSurvived,
        @JsonProperty("isAlive") boolean isAlive,
        String causeOfDeath
) {}
