package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.LogRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;
import io.github.gabrielpetry23.logscopeapi.security.CustomUserDetails;
import io.github.gabrielpetry23.logscopeapi.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COMPANY_SYSTEM', 'COMPANY_ADMIN', 'GLOBAL_ADMIN')")
    public ResponseEntity<Void> createLog(@Valid @RequestBody LogRequestDTO logRequestDTO, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        logService.saveLog(userDetails.getUsername(), logRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COMPANY_ADMIN', 'COMPANY_VIEWER', 'GLOBAL_ADMIN', 'GLOBAL_SUPPORT')")
    public ResponseEntity<List<LogResponseDTO>> getLogs(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(required = false) String messageContains,
            Authentication authentication) {


        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<LogResponseDTO> logs = logService.getLogsForUserWithFilters(
                userDetails.getUsername(),
                level,
                start,
                end,
                messageContains
        );

        System.out.println("Logs encontrados: " + logs.size());
        return ResponseEntity.ok(logs);
    }
}


