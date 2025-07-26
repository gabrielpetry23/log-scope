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
@Document(collection = "alerts")
public class Alert {
    @Id
    private String id;
    private String ruleId;
    private LocalDateTime timestamp;
    private String message;
    private List<String> notifiedChannels;
}