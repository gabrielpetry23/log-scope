package io.github.gabrielpetry23.logscopeapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ClientRegistrationRequestDTO(
        @NotBlank
        String companyName,
        @NotBlank
        @Email
        String contactEmail,
        @NotBlank
        @Size(min = 8)
        String initialAdminPassword
) {
}
