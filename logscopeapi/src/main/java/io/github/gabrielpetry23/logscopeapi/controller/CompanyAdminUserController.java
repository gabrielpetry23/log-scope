package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.config.TenantContext;
import io.github.gabrielpetry23.logscopeapi.controller.mapper.UserMapper;
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
    private final UserMapper userMapper;

    @PostMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<UserResponseDTO> createUserForCompany(@Valid @RequestBody UserCreationRequestDTO request) {

        User newUser = userService.createUserForCompany(request);

        UserResponseDTO responseDTO = userMapper.toDTO(newUser);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('COMPANY_ADMIN')")
    public ResponseEntity<List<UserResponseDTO>> getUsersByCompany() {
        List<User> users = userService.getUsersByCompany();

        List<UserResponseDTO> responseDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseDTOs);
    }
}
