package io.github.gabrielpetry23.logscopeapi.repository;

import io.github.gabrielpetry23.logscopeapi.dto.LogResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface LogRepositoryCustom {
    List<LogResponseDTO> findByUserIdWithFilters(String userId, String level, LocalDateTime start, LocalDateTime end, String messageContains);
}

