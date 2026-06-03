package com.elrey.backend.repository;

import com.elrey.backend.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameSessionRepository extends JpaRepository<GameSession, Long> {
    List<GameSession> findAllByOrderByStartedAtDesc();
}
