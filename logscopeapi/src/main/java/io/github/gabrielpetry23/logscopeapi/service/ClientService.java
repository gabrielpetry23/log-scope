package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.model.Client;
import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.model.enums.Role;
import io.github.gabrielpetry23.logscopeapi.repository.ClientRepository;
import io.github.gabrielpetry23.logscopeapi.security.CustomUserDetails;
import io.github.gabrielpetry23.logscopeapi.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public String authenticateAndGenerateJwt(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return jwtUtils.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
            return null;
        }
    }

    public Client registerNewClient(String companyName, String contactEmail, String initialAdminPassword) {
        Client client = new Client();
        client.setCompanyName(companyName);
        client.setContactEmail(contactEmail);
        client.setClientSecretHash(passwordEncoder.encode(initialAdminPassword));
        client.setRoles(Set.of(Role.COMPANY_SYSTEM));
        client.setCreatedAt(Instant.now());
        client.setLastModifiedAt(Instant.now());
        Client savedClient = clientRepository.save(client);

        User adminUser = User.builder()
                .username(contactEmail)
                .password(initialAdminPassword)
                .roles(Set.of(Role.COMPANY_ADMIN))
                .clientId(savedClient.getClientId())
                .build();

        userService.save(adminUser);

        return savedClient;
    }

    public Optional<Client> findByClientId(String clientId) {
        return clientRepository.findByClientId(clientId);
    }

    public String generateNewClientSecret(String clientId) {
        Client client = findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        String newSecret = UUID.randomUUID().toString();
        client.setClientSecretHash(passwordEncoder.encode(newSecret));
        client.setLastModifiedAt(Instant.now());
        clientRepository.save(client);
        return newSecret;
    }
}
