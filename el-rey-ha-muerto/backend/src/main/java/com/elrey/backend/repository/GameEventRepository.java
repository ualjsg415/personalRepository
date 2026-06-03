package com.elrey.backend.repository;

import com.elrey.backend.entity.GameEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface GameEventRepository extends JpaRepository<GameEvent, Long> {
    Optional<GameEvent> findFirstByDayTarget(Integer dayTarget);

    @Query(value = "SELECT * FROM game_events ORDER BY RANDOM() LIMIT 1", nativeQuery = true)
    Optional<GameEvent> findRandom();
}
