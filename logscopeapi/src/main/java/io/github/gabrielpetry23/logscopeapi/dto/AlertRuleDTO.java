package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AlertRuleDTO(
        String id,
        
        @NotBlank(message = "Rule name cannot be blank")
        String name,
        
        @NotBlank(message = "Application name cannot be blank")
        String application,
        
        String environment,
        
        @NotBlank(message = "Log level cannot be blank")
        String level,
        
        @NotBlank(message = "Match pattern cannot be blank")
        String matchPattern,
        
        @NotNull(message = "Threshold cannot be null")
        @Min(value = 1, message = "Threshold must be at least 1")
        Integer threshold,
        
        @NotNull(message = "Interval cannot be null")
        @Min(value = 1, message = "Interval must be at least 1 second")
        Integer intervalSeconds,
        
        @NotNull(message = "Notification channels cannot be null")
        List<String> notificationChannels,
        
        Boolean enabled,
        
        String userId,
        
        String clientId
) {
    public AlertRuleDTO {
        if (enabled == null) {
            enabled = true;
        }
        if (notificationChannels == null) {
            notificationChannels = List.of("email");
        }
    }
}
