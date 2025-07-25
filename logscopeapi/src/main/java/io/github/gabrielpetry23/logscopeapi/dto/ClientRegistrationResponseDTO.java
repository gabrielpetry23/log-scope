package io.github.gabrielpetry23.logscopeapi.dto;

public record ClientRegistrationResponseDTO(
        String message,
        String clientId,
        String initialAdminUsername,
        String jwtForNewClient
) {
}
