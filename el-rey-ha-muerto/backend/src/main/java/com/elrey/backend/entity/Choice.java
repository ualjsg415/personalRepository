package com.elrey.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "choices")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Choice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private GameEvent event;

    @Column(nullable = false, length = 1)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "hidden_flag")
    private String hiddenFlag;

    @Column(name = "flag_trigger_delay", nullable = false)
    private Integer flagTriggerDelay;

    @Column(name = "stat_hygiene", nullable = false)
    private Integer statHygiene;

    @Column(name = "stat_hunger", nullable = false)
    private Integer statHunger;

    @Column(name = "stat_popularity", nullable = false)
    private Integer statPopularity;

    @Column(name = "stat_wealth", nullable = false)
    private Integer statWealth;

    @Column(name = "immediate_death", nullable = false)
    private Boolean immediateDeath;

    @Column(name = "death_message", columnDefinition = "TEXT")
    private String deathMessage;
}
