package io.github.gabrielpetry23.logscopeapi.model;


import io.github.gabrielpetry23.logscopeapi.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder()
public class User {

    @Id
    @Builder.Default
    private String id = null;

    @Indexed(unique = true)
    private String username;

    private String password;

    private Set<Role> roles;

    private String clientId;
}

