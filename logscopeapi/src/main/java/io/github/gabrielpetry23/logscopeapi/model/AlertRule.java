package io.github.gabrielpetry23.logscopeapi.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "alert_rules")
public class AlertRule {
    @Id
    private String id;
    
    private String name;
    
    private String application;
    
    private String environment;
    
    private String level;
    
    private String matchPattern;
    
    private Integer threshold;
    
    private Integer intervalSeconds;
    
    private List<String> notificationChannels;
    
    @Builder.Default
    private Boolean enabled = true;
    
    private String userId;
    
    private String clientId;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
}