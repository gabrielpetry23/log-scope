package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.config.TenantContext;
import io.github.gabrielpetry23.logscopeapi.dto.UserCreationRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.UserResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/company-admin/users")
@RequiredArgsConstructor
public class CompanyAdminUserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponseDTO> createUserForCompany(@Valid @RequestBody UserCreationRequestDTO request) {
        String currentClientId = TenantContext.getTenantId();
        if (currentClientId == null || currentClientId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setPassword(request.password());
        newUser.setRoles(request.roles());
        newUser.setClientId(currentClientId);

        User savedUser = userService.save(newUser);

        UserResponseDTO responseDTO = new UserResponseDTO(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getRoles(),
                savedUser.getClientId()
        );

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByCompany() {
        String currentClientId = TenantContext.getTenantId();
        if (currentClientId == null || currentClientId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        List<User> users = userService.findByClientId(currentClientId);

        List<UserResponseDTO> responseDTOs = users.stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getRoles(),
                        user.getClientId()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }
}
