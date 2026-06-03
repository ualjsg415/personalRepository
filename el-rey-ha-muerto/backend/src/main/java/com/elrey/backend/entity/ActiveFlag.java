package com.elrey.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "active_flags")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ActiveFlag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "flag_name", nullable = false)
    private String flagName;

    @Column(name = "trigger_day", nullable = false)
    private Integer triggerDay;

    @Column(name = "death_message", columnDefinition = "TEXT")
    private String deathMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
