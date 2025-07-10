package io.github.gabrielpetry23.logscopeapi.service;

import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.model.enums.Role;
import io.github.gabrielpetry23.logscopeapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findByClientId(String clientId) {
        return userRepository.findByClientId(clientId);
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    @PostConstruct
    public void initGlobalAdmins() {
        if (userRepository.findByUsername("logscope_admin").isEmpty()) {
            User admin = User.builder()
                    .username("logscope_admin")
                    .password("super-admin-pass")
                    .roles(Set.of(Role.GLOBAL_ADMIN, Role.GLOBAL_SUPPORT))
                    .build();
            save(admin);
            System.out.println("GLOBAL ADMIN 'logscope_admin' created.");
        }
    }
}
