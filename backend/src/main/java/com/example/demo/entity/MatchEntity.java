package com.example.demo.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
public class MatchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matchup", nullable = false)
    private String matchup;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameEntity> games = new ArrayList<>();

    public MatchEntity() {
    }

    public MatchEntity(String matchup) {
        this.matchup = matchup;
    }

    public Long getId() {
        return id;
    }

    public String getMatchup() {
        return matchup;
    }

    public void setMatchup(String matchup) {
        this.matchup = matchup;
    }

    public List<GameEntity> getGames() {
        return games;
    }

    public void addGame(GameEntity game) {
        game.setMatch(this);
        this.games.add(game);
    }

    public void clearGames() {
        this.games.clear();
    }
}
