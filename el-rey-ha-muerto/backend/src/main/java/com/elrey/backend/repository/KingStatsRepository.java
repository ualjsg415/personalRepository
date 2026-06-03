package com.elrey.backend.repository;

import com.elrey.backend.entity.KingStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface KingStatsRepository extends JpaRepository<KingStats, Long> {
    Optional<KingStats> findTopBySessionIdOrderByDayDesc(Long sessionId);
}
