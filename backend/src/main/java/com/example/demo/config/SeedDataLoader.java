package com.example.demo.config;

import com.example.demo.entity.GameEntity;
import com.example.demo.entity.MatchEntity;
import com.example.demo.entity.PlayerStatEntity;
import com.example.demo.repository.MatchRepository;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class SeedDataLoader implements CommandLineRunner {

    private final MatchRepository matchRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    public SeedDataLoader(MatchRepository matchRepository, ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.matchRepository = matchRepository;
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (matchRepository.count() > 0) {
            return;
        }

        List<SeedMatch> seedMatches = loadSeedMatches();
        for (SeedMatch seedMatch : seedMatches) {
            MatchEntity match = new MatchEntity(seedMatch.getEnfrentamiento());

            for (SeedGame seedGame : seedMatch.getGames()) {
                GameEntity game = new GameEntity(seedGame.getGameTitulo());
                int splitIndex = (int) Math.ceil(seedGame.getJugadores().size() / 2.0);

                for (int i = 0; i < seedGame.getJugadores().size(); i++) {
                    SeedPlayer player = seedGame.getJugadores().get(i);
                    int teamIndex = i < splitIndex ? 0 : 1;

                    PlayerStatEntity stat = new PlayerStatEntity(
                            player.getNombre(),
                            player.getRol(),
                            player.getKills(),
                            player.getDeaths(),
                            player.getAssists(),
                            player.getCs(),
                            player.getVisionScore(),
                            teamIndex,
                            i
                    );

                    game.addPlayer(stat);
                }

                match.addGame(game);
            }

            matchRepository.save(match);
        }
    }

    private List<SeedMatch> loadSeedMatches() throws IOException {
        Resource resource = resourceLoader.getResource("classpath:seed/lck_fullstats_2026.json");
        try (InputStream input = resource.getInputStream()) {
            return objectMapper.readValue(input, new TypeReference<List<SeedMatch>>() {
            });
        }
    }

    private static class SeedMatch {
        private String enfrentamiento;
        private List<SeedGame> games;

        public String getEnfrentamiento() {
            return enfrentamiento;
        }

        public List<SeedGame> getGames() {
            return games;
        }
    }

    private static class SeedGame {
        @JsonProperty("game_titulo")
        private String gameTitulo;

        private List<SeedPlayer> jugadores;

        public String getGameTitulo() {
            return gameTitulo;
        }

        public List<SeedPlayer> getJugadores() {
            return jugadores;
        }
    }

    private static class SeedPlayer {
        private String nombre;
        private String rol;
        private int kills;
        private int deaths;
        private int assists;
        private int cs;

        @JsonProperty("vision_score")
        private int visionScore;

        public String getNombre() {
            return nombre;
        }

        public String getRol() {
            return rol;
        }

        public int getKills() {
            return kills;
        }

        public int getDeaths() {
            return deaths;
        }

        public int getAssists() {
            return assists;
        }

        public int getCs() {
            return cs;
        }

        public int getVisionScore() {
            return visionScore;
        }
    }
}
