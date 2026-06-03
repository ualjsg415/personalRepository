package com.elrey.backend.repository;

import com.elrey.backend.entity.ActiveFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActiveFlagRepository extends JpaRepository<ActiveFlag, Long> {
    List<ActiveFlag> findBySessionIdAndTriggerDay(Long sessionId, Integer triggerDay);
}
