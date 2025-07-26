package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.AlertTestRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.AlertTestResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Alert;
import io.github.gabrielpetry23.logscopeapi.repository.AlertRepository;
import io.github.gabrielpetry23.logscopeapi.service.AlertTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertRepository alertRepository;
    private final AlertTestService alertTestService;

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_SUPPORT')")
    public ResponseEntity<List<Alert>> getAlertHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        
        List<Alert> alerts;
        if (start != null && end != null) {
            alerts = alertRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
        } else {
            alerts = alertRepository.findAllByOrderByTimestampDesc();
        }
        
        return ResponseEntity.ok(alerts);
    }

    @GetMapping("/rule/{ruleId}")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_SUPPORT')")
    public ResponseEntity<List<Alert>> getAlertsByRule(@PathVariable String ruleId) {
        List<Alert> alerts = alertRepository.findByRuleIdOrderByTimestampDesc(ruleId);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('GLOBAL_ADMIN', 'COMPANY_ADMIN')")
    public ResponseEntity<AlertTestResponseDTO> testAlert(@Valid @RequestBody AlertTestRequestDTO dto) {
        AlertTestResponseDTO result = alertTestService.simulateLog(
            dto.application(), 
            dto.logSample(), 
            dto.level(), 
            dto.environment()
        );
        return ResponseEntity.ok(result);
    }
}

