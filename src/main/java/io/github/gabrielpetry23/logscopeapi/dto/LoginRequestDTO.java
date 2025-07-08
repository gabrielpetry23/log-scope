package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank
        String username,
        @NotBlank
        String password
) {
}
