package io.github.gabrielpetry23.logscopeapi.dto;


import java.util.List;

public record JwtResponseDTO(
        String token,
        String type, // Não precisa de = "Bearer", pode ser injetado no construtor se preferir
        String username,
        List<String> roles,
        String clientId
) {

    public JwtResponseDTO(String token, String username, List<String> roles, String clientId) {
        this(token, "Bearer", username, roles, clientId);
    }
}
