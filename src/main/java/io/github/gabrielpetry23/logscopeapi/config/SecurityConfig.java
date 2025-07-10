package io.github.gabrielpetry23.logscopeapi.config;

import io.github.gabrielpetry23.logscopeapi.security.CustomUserDetailsService;
import io.github.gabrielpetry23.logscopeapi.security.JwtUtils;
import io.github.gabrielpetry23.logscopeapi.security.JwtAuthorizationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtils jwtUtils;
    private final TenantFilter tenantFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return authManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/oauth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/oauth/token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/oauth/register-client").hasRole("GLOBAL_ADMIN")
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/logs").hasAnyRole("COMPANY_SYSTEM", "COMPANY_ADMIN", "GLOBAL_ADMIN")
                        .requestMatchers("/api/v1/rules/**").hasAnyRole("COMPANY_ADMIN", "GLOBAL_ADMIN")
                        .requestMatchers("/api/v1/alerts/**").hasAnyRole("COMPANY_ADMIN", "COMPANY_VIEWER", "GLOBAL_ADMIN", "GLOBAL_SUPPORT")
                        .requestMatchers("/api/v1/admin/users/**").hasAnyRole("COMPANY_ADMIN", "GLOBAL_ADMIN")
                        .requestMatchers("/api/v1/global-admin/**").hasRole("GLOBAL_ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthorizationFilter(jwtUtils, customUserDetailsService), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, JwtAuthorizationFilter.class);

        return http.build();
    }
}