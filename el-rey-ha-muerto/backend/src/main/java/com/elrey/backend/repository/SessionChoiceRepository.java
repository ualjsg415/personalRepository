package com.elrey.backend.repository;

import com.elrey.backend.entity.SessionChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SessionChoiceRepository extends JpaRepository<SessionChoice, Long> {
    List<SessionChoice> findBySessionIdOrderByDayAsc(Long sessionId);
}
