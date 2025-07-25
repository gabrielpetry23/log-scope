package io.github.gabrielpetry23.logscopeapi.security;

import io.github.gabrielpetry23.logscopeapi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

//    public String generateToken(User user) {
//        String roles = user.getRoles().stream()
//                .map(Enum::name)
//                .collect(Collectors.joining(","));
//
//        return Jwts.builder()
//                .subject(user.getUsername())
//                .claim("roles", roles)
//                .issuedAt(new Date())
//                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
//                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
//                .compact();
//    }

    public String generateToken(CustomUserDetails userDetails) { // << ALTERADO AQUI
        String roles = userDetails.getAuthorities().stream() // << Pega as authorities do UserDetails
                .map(GrantedAuthority::getAuthority) // << Transforma em String
                .map(role -> role.replace("ROLE_", "")) // Remove o prefixo "ROLE_" para os claims
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("clientId", userDetails.getClientId()) // Opcional: Adicionar clientId no token
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }
}