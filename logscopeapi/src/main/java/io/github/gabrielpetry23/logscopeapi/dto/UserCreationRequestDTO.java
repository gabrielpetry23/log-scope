package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.github.gabrielpetry23.logscopeapi.model.enums.Role;

import java.util.Set;

public record UserCreationRequestDTO(
        @NotBlank(message = "Username cannot be empty")
        @Email(message = "Username must be a valid email format")
        String username,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password,

        Set<Role> roles
) {}
