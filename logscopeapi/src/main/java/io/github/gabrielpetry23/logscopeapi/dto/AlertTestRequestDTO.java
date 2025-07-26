package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.NotBlank;

public record AlertTestRequestDTO(
        @NotBlank(message = "Application cannot be blank")
        String application,
        
        @NotBlank(message = "Log sample cannot be blank")
        String logSample,
        
        String level,
        String environment
) {
    public AlertTestRequestDTO {
        if (level == null || level.trim().isEmpty()) {
            level = "ERROR";
        }
        if (environment == null || environment.trim().isEmpty()) {
            environment = "prod";
        }
    }
}
