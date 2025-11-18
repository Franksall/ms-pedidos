package com.example.ms_pedidos;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        // Protege TODOS los endpoints
                        .anyExchange().authenticated()
                )
                // Configura el servicio para que sea un Resource Server y valide JWTs
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        // Deshabilitar CSRF porque es una API stateless (basada en tokens)
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }
}