package com.example.demo.controller;

import com.example.demo.dto.MatchDtos.GameResponse;
import com.example.demo.dto.MatchDtos.MatchResponse;
import com.example.demo.dto.MatchDtos.PlayerResponse;
import com.example.demo.entity.GameEntity;
import com.example.demo.entity.MatchEntity;
import com.example.demo.entity.PlayerStatEntity;
import com.example.demo.repository.MatchRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/matches")
@CrossOrigin(origins = "*")
public class MatchController {

    private final MatchRepository matchRepository;

    public MatchController(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(@RequestParam(name = "team", required = false) String team) {
        List<MatchEntity> matches = (team == null || team.isBlank())
                ? matchRepository.findAll()
                : matchRepository.findByMatchupContainingIgnoreCase(team);

        return matches.stream()
                .sorted(Comparator.comparing(MatchEntity::getId))
                .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<MatchResponse> getMatchById(@PathVariable Long id) {
        return matchRepository.findById(id)
                .map(match -> ResponseEntity.ok(toResponse(match)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private MatchResponse toResponse(MatchEntity match) {
        List<GameResponse> games = match.getGames().stream()
                .sorted(Comparator.comparing(GameEntity::getId))
                .map(this::toGameResponse)
            .collect(Collectors.toList());

        return new MatchResponse(match.getId(), match.getMatchup(), games);
    }

    private GameResponse toGameResponse(GameEntity game) {
        List<PlayerResponse> players = game.getPlayers().stream()
                .sorted(Comparator.comparing(PlayerStatEntity::getPlayerOrder))
                .map(this::toPlayerResponse)
            .collect(Collectors.toList());

        return new GameResponse(game.getTitle(), players);
    }

    private PlayerResponse toPlayerResponse(PlayerStatEntity player) {
        return new PlayerResponse(
                player.getName(),
                player.getRole(),
                player.getKills(),
                player.getDeaths(),
                player.getAssists(),
                player.getCs(),
                player.getVisionScore()
        );
    }
}
