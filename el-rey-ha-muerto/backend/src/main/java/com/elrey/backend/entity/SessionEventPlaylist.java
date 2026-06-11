package com.elrey.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "session_event_playlist")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SessionEventPlaylist {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "event_id", nullable = false)
    private Long eventId;
}
