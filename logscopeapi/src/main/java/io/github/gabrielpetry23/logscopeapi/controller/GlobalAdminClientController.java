package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.ClientRegistrationRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.ClientRegistrationResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Client;
import io.github.gabrielpetry23.logscopeapi.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/global-admin/clients")
@RequiredArgsConstructor
public class GlobalAdminClientController {

    private final ClientService clientService;

    @PostMapping
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<ClientRegistrationResponseDTO> registerClient(@Valid @RequestBody ClientRegistrationRequestDTO request) {
        Client newClient = clientService.registerNewClient(
                request.companyName(),
                request.contactEmail(),
                request.initialAdminPassword()
        );

        String initialAdminUsername = request.contactEmail();
        String jwtForNewClient = clientService.authenticateAndGenerateJwt(initialAdminUsername, request.initialAdminPassword());

        String successMessage = "Client '" + newClient.getCompanyName() + "' registered. Client ID: " + newClient.getClientId() +
                ". Initial Admin Username: " + initialAdminUsername + ". Please communicate the initial password securely to the client.";
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ClientRegistrationResponseDTO(successMessage, newClient.getClientId(), initialAdminUsername, jwtForNewClient)
        );
    }
}