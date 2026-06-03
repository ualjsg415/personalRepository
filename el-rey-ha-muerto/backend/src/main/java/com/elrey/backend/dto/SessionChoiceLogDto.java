package com.elrey.backend.dto;

public record SessionChoiceLogDto(
        int day,
        String eventTitle,
        String choiceText,
        boolean hasHiddenFlag,
        String deathMessage
) {}
