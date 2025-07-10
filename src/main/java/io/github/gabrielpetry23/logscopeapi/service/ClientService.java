package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.model.Client;
import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.model.enums.Role;
import io.github.gabrielpetry23.logscopeapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
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

//    @PostConstruct
//    public void init() {
//        // Exemplo para Client 001
//        String testClientId1 = "logscope-client-001";
//        String initialSecret1 = "test-secret-001";
//        if (clientRepository.findByClientId(testClientId1).isEmpty()) {
//            Client client1 = new Client(); // Cria nova instância
//            client1.setClientId(testClientId1); // Define o clientId fixo
//            client1.setCompanyName("Acme Corp");
//            client1.setContactEmail("contact@acme.com");
//            client1.setClientSecretHash(passwordEncoder.encode(initialSecret1)); // Hashea a senha
//            client1.setRoles(Set.of(Role.COMPANY_SYSTEM)); // Exemplo de role mais adequada para client_credentials
//            client1.setEnabled(true);
//            client1.setCreatedAt(Instant.now());
//            client1.setLastModifiedAt(Instant.now());
//            clientRepository.save(client1);
//            System.out.println("Test Client 'Acme Corp' created with Client ID: " + testClientId1 + " and Secret: " + initialSecret1);
//        }
//
//        // Repetir para logscope-client-002 de forma similar
//        String testClientId2 = "logscope-client-002";
//        String initialSecret2 = "test-secret-002";
//        if (clientRepository.findByClientId(testClientId2).isEmpty()) {
//            Client client2 = new Client();
//            client2.setClientId(testClientId2);
//            client2.setCompanyName("Globex Inc");
//            client2.setContactEmail("support@globex.com");
//            client2.setClientSecretHash(passwordEncoder.encode(initialSecret2));
//            client2.setRoles(Set.of(Role.COMPANY_SYSTEM));
//            client2.setEnabled(true);
//            client2.setCreatedAt(Instant.now());
//            client2.setLastModifiedAt(Instant.now());
//            clientRepository.save(client2);
//            System.out.println("Test Client 'Globex Inc' created with Client ID: " + testClientId2 + " and Secret: " + initialSecret2);
//        }
//    }
}
