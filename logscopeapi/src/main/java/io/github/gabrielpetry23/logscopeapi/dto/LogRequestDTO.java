package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LogRequestDTO(
        @NotBlank String message,
        @NotBlank String level,
        @NotBlank String source
) {
}
