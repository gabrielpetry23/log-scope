package io.github.gabrielpetry23.logscopeapi.model;

import io.github.gabrielpetry23.logscopeapi.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clients")
public class Client {
    @Id
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    @Indexed(unique = true)
    @Builder.Default
    private String clientId = UUID.randomUUID().toString();

    private String clientSecretHash;
    private String companyName;
    private String contactEmail;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private Instant createdAt = Instant.now();

    @Builder.Default
    private Instant lastModifiedAt = Instant.now();

    private Set<Role> roles;
}
