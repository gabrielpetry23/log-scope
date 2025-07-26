package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepositoryCustom {
    List<LogResponseDTO> findByUserIdWithFilters(String userId, String level, LocalDateTime start, LocalDateTime end, String messageContains);
    List<LogResponseDTO> findByClientIdWithFilters(String clientId, String level, LocalDateTime start, LocalDateTime end, String messageContains);
    List<LogResponseDTO> findByApplicationWithFilters(String clientId, String application, String level, LocalDateTime start, LocalDateTime end);
}

