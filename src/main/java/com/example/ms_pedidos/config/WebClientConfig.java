package com.example.ms_pedidos.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()

                .filter(logRequest())
                .filter(propagateTokenFilter());
    }


    private ExchangeFilterFunction propagateTokenFilter() {
        return (clientRequest, next) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    // busca el token
                    if (context.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        String tokenValue = jwtAuth.getToken().getTokenValue();

                        // pone en la cabecera de la petición saliente
                        ClientRequest filteredRequest = ClientRequest.from(clientRequest)
                                .header("Authorization", "Bearer " + tokenValue)
                                .build();
                        return next.exchange(filteredRequest);
                    }
                    return next.exchange(clientRequest);
                });
    }

    // filtro para ver qué está haciendo el WebClient en los logs
    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            System.out.println("WebClient Request: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> System.out.println(name + ": " + value)));
            return next.exchange(clientRequest);
        };
    }
}