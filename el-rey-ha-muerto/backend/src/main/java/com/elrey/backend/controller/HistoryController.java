package com.elrey.backend.controller;

import com.elrey.backend.dto.SessionDetailDto;
import com.elrey.backend.dto.SessionSummaryDto;
import com.elrey.backend.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<List<SessionSummaryDto>> getAll() {
        return ResponseEntity.ok(historyService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDetailDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(historyService.getSessionDetail(id));
    }
}
