package io.github.gabrielpetry23.logscopeapi.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record LogResponseDTO(
        String id,
        LocalDateTime timestamp,
        String level,
        String application,
        String environment,
        String message,
        String hostname,
        String clientId,
        Map<String, Object> metadata
) {
}
