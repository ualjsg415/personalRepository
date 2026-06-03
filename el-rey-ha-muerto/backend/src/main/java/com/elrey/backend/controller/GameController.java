package com.elrey.backend.controller;

import com.elrey.backend.dto.ChoiceRequest;
import com.elrey.backend.dto.GameStateDto;
import com.elrey.backend.dto.StartGameRequest;
import com.elrey.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<GameStateDto> startGame(@RequestBody StartGameRequest request) {
        return ResponseEntity.ok(gameService.startGame(request.playerName()));
    }

    @GetMapping("/{sessionId}/state")
    public ResponseEntity<GameStateDto> getState(@PathVariable Long sessionId) {
        return ResponseEntity.ok(gameService.getState(sessionId));
    }

    @PostMapping("/{sessionId}/choose")
    public ResponseEntity<GameStateDto> choose(@PathVariable Long sessionId,
                                               @RequestBody ChoiceRequest request) {
        return ResponseEntity.ok(gameService.processChoice(sessionId, request.choiceId()));
    }
}
