package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.ClientRegistrationRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.ClientRegistrationResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.Client;
import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.security.CustomUserDetails;
import io.github.gabrielpetry23.logscopeapi.security.JwtUtils;
import io.github.gabrielpetry23.logscopeapi.service.ClientService;
import io.github.gabrielpetry23.logscopeapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/global-admin/clients")
@RequiredArgsConstructor
public class GlobalAdminClientController {

    private final ClientService clientService;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping
    @PreAuthorize("hasRole('GLOBAL_ADMIN')")
    public ResponseEntity<ClientRegistrationResponseDTO> registerClient(@Valid @RequestBody ClientRegistrationRequestDTO request) {
        Client newClient = clientService.registerNewClient(
                request.companyName(),
                request.contactEmail(),
                request.initialAdminPassword()
        );

        String initialAdminUsername = request.contactEmail();
        String jwtForNewClient = null;

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(initialAdminUsername, request.initialAdminPassword()));
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            jwtForNewClient = jwtUtils.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            System.err.println("Error authenticating the initial admin of the new client: " + e.getMessage());
        }

        String successMessage = "Client '" + newClient.getCompanyName() + "' registered. Client ID: " + newClient.getClientId() +
                ". Initial Admin Username: " + initialAdminUsername + ". Please communicate the initial password securely to the client.";
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ClientRegistrationResponseDTO(successMessage, newClient.getClientId(), initialAdminUsername, jwtForNewClient)
        );
    }
}