package io.github.gabrielpetry23.logscopeapi.dto;

import io.github.gabrielpetry23.logscopeapi.model.enums.Role;

import java.util.Set;

public record UserResponseDTO(
        String id,
        String username,
        Set<Role> roles,
        String clientId
) {
}
