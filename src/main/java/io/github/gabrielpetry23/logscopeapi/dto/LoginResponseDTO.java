package io.github.gabrielpetry23.logscopeapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponseDTO(
        String token, String message
) {
    public LoginResponseDTO(String token) {
        this(token, null);
    }
    public LoginResponseDTO(String token, String message) {
        this.token = token;
        this.message = message;
    }
}
