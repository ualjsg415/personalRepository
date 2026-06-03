package com.elrey.backend.dto;

import java.util.List;

public record GameEventDto(Long id, String title, String description, String scene, List<ChoiceDto> choices) {}
