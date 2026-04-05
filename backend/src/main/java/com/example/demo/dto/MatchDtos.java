package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class MatchDtos {

    private MatchDtos() {
    }

    public static class MatchResponse {
        private final Long id;
        private final String enfrentamiento;
        private final List<GameResponse> games;

        public MatchResponse(Long id, String enfrentamiento, List<GameResponse> games) {
            this.id = id;
            this.enfrentamiento = enfrentamiento;
            this.games = games;
        }

        public Long getId() {
            return id;
        }

        public String getEnfrentamiento() {
            return enfrentamiento;
        }

        public List<GameResponse> getGames() {
            return games;
        }
    }

    public static class GameResponse {
        @JsonProperty("game_titulo")
        private final String gameTitulo;

        private final List<PlayerResponse> jugadores;

        public GameResponse(String gameTitulo, List<PlayerResponse> jugadores) {
            this.gameTitulo = gameTitulo;
            this.jugadores = jugadores;
        }

        public String getGameTitulo() {
            return gameTitulo;
        }

        public List<PlayerResponse> getJugadores() {
            return jugadores;
        }
    }

    public static class PlayerResponse {
        private final String nombre;
        private final String rol;
        private final int kills;
        private final int deaths;
        private final int assists;
        private final int cs;

        @JsonProperty("vision_score")
        private final int visionScore;

        public PlayerResponse(String nombre, String rol, int kills, int deaths, int assists, int cs, int visionScore) {
            this.nombre = nombre;
            this.rol = rol;
            this.kills = kills;
            this.deaths = deaths;
            this.assists = assists;
            this.cs = cs;
            this.visionScore = visionScore;
        }

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
