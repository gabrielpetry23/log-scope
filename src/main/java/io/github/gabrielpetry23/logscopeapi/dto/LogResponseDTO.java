package io.github.gabrielpetry23.logscopeapi.dto;

import java.time.LocalDateTime;

public record LogResponseDTO(
        String id,
        String message,
        String level,
        String source,
        LocalDateTime timestamp
) {
}
