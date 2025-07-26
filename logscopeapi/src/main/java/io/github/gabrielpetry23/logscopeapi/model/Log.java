package io.github.gabrielpetry23.logscopeapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {
    @Id
    private String id;

    @Builder.Default
    @Indexed
    private LocalDateTime timestamp = LocalDateTime.now();

    @Indexed
    private String level;

    @Indexed
    private String application;

    @Indexed
    private String environment;

    private String message;

    private String hostname;

    @Indexed
    private String clientId;

    private String userId; // Keep for compatibility

    private String source; // Keep for compatibility

    private Map<String, Object> metadata;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
