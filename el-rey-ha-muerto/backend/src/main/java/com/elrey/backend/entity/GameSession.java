package com.elrey.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_sessions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GameSession {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "days_survived", nullable = false)
    private Integer daysSurvived;

    @Column(name = "is_alive", nullable = false)
    private Boolean isAlive;

    @Column(name = "cause_of_death")
    private String causeOfDeath;
}
