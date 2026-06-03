package com.elrey.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "king_stats")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class KingStats {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private Integer day;

    @Column(nullable = false)
    private Integer hygiene;

    @Column(nullable = false)
    private Integer hunger;

    @Column(nullable = false)
    private Integer popularity;

    @Column(nullable = false)
    private Integer wealth;
}
