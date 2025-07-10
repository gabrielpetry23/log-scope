package io.github.gabrielpetry23.logscopeapi.security;

import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.repository.ClientRepository;
import io.github.gabrielpetry23.logscopeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository; // Adiciona ClientRepository para buscar Clients

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User: " + username + " not found!"));
//        return CustomUserDetails.build(user);
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Tenta carregar como um User (para login de humanos)
        return userRepository.findByUsername(username)
                .map(CustomUserDetails::build) // Se encontrar um User, constrói CustomUserDetails
                .orElseGet(() -> { // Se não encontrar User, tenta carregar como Client
                    // 2. Tenta carregar como um Client (para client_credentials)
                    return clientRepository.findByClientId(username) // << Usa findByClientId aqui
                            .map(CustomUserDetails::buildFromClient) // Se encontrar um Client, constrói CustomUserDetails
                            .orElseThrow(() -> new UsernameNotFoundException("User or Client with ID/Username: " + username + " not found!"));
                });
    }
}
