package com.example.demo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "player_stats")
public class PlayerStatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "kills", nullable = false)
    private int kills;

    @Column(name = "deaths", nullable = false)
    private int deaths;

    @Column(name = "assists", nullable = false)
    private int assists;

    @Column(name = "cs", nullable = false)
    private int cs;

    @Column(name = "vision_score", nullable = false)
    private int visionScore;

    @Column(name = "team_index", nullable = false)
    private int teamIndex;

    @Column(name = "player_order", nullable = false)
    private int playerOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    public PlayerStatEntity() {
    }

    public PlayerStatEntity(String name, String role, int kills, int deaths, int assists, int cs, int visionScore, int teamIndex, int playerOrder) {
        this.name = name;
        this.role = role;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.cs = cs;
        this.visionScore = visionScore;
        this.teamIndex = teamIndex;
        this.playerOrder = playerOrder;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
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

    public int getTeamIndex() {
        return teamIndex;
    }

    public int getPlayerOrder() {
        return playerOrder;
    }

    public GameEntity getGame() {
        return game;
    }

    public void setGame(GameEntity game) {
        this.game = game;
    }
}
