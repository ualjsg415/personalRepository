package com.example.demo.repository;

import com.example.demo.entity.MatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    List<MatchEntity> findByMatchupContainingIgnoreCase(String team);
}
