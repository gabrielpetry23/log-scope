package io.github.gabrielpetry23.logscopeapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.gabrielpetry23.logscopeapi.config.CustomLocalDateTimeDeserializer;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.Map;

public record LogRequestDTO(
        @JsonDeserialize(using = CustomLocalDateTimeDeserializer.class)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        
        @NotBlank(message = "Level cannot be blank")
        String level,
        
        @NotBlank(message = "Application cannot be blank")
        String application,
        
        String environment,
        
        @NotBlank(message = "Message cannot be blank")
        String message,
        
        String hostname,
        
        Map<String, Object> metadata
) {
    public LogRequestDTO {
        // Set default timestamp if not provided
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        
        // Set default environment if not provided
        if (environment == null || environment.trim().isEmpty()) {
            environment = "unknown";
        }
    }
}
