package com.elrey.backend.repository;

import com.elrey.backend.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    Optional<GameEvent> findFirstByDayTarget(Integer dayTarget);
    Optional<GameEvent> findFirstByDayTargetAndSourceOrderByScrapedAtDesc(Integer dayTarget, String source);
    Optional<GameEvent> findTopBySourceOrderByScrapedAtDesc(String source);

    long countBySource(String source);
    List<GameEvent> findAllBySource(String source);

    @Modifying
    @Transactional
    @Query("DELETE FROM GameEvent e WHERE e.source = :source")
    void deleteAllBySource(@Param("source") String source);

    @Query(value = "SELECT * FROM game_events ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<GameEvent> findRandom();
}
