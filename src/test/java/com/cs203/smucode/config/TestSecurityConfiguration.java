package com.cs203.smucode.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.util.Map;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfiguration {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(STATELESS)
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return token -> {
            return new Jwt(token, Instant.now(), Instant.now().plusSeconds(300),
                    Map.of("alg", "RS256"), Map.of("sub", "testuser", "scope", "ROLE_USER"));
        };
    }
}