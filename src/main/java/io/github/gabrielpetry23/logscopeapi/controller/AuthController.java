package io.github.gabrielpetry23.logscopeapi.controller;

import io.github.gabrielpetry23.logscopeapi.dto.JwtResponseDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LoginRequestDTO;
import io.github.gabrielpetry23.logscopeapi.dto.LoginResponseDTO;
import io.github.gabrielpetry23.logscopeapi.model.User;
import io.github.gabrielpetry23.logscopeapi.security.CustomUserDetails;
import io.github.gabrielpetry23.logscopeapi.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtenha o CustomUserDetails do objeto Authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        String jwt = jwtUtils.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDTO(jwt, userDetails.getUsername(), roles, userDetails.getClientId()));
    }

    @PostMapping("/token")
    public ResponseEntity<LoginResponseDTO> getToken(@RequestParam("grant_type") String grantType,
                                                     @RequestParam("client_id") String clientId,
                                                     @RequestParam("client_secret") String clientSecret) {
        if (!"client_credentials".equals(grantType)) {
            return ResponseEntity.badRequest().body(new LoginResponseDTO(null, "Unsupported grant_type"));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(clientId, clientSecret));

        CustomUserDetails clientDetails = (CustomUserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(clientDetails);

        return ResponseEntity.ok(new LoginResponseDTO(jwt));
    }
}