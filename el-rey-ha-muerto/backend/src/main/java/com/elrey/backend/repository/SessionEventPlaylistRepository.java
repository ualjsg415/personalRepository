package com.elrey.backend.repository;

import com.elrey.backend.entity.SessionEventPlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionEventPlaylistRepository extends JpaRepository<SessionEventPlaylist, Long> {
    Optional<SessionEventPlaylist> findBySessionIdAndPosition(Long sessionId, Integer position);
}
