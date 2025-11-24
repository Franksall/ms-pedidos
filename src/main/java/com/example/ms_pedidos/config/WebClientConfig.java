package com.example.ms_pedidos.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean(name = "plainWebClientBuilder")
    public WebClient.Builder plainWebClientBuilder() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create().responseTimeout(Duration.ofSeconds(10))
                ));

    }

    private ExchangeFilterFunction propagateTokenFilter() {
        return (clientRequest, next) -> ReactiveSecurityContextHolder.getContext()
                .flatMap(context -> {
                    if (context.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        String tokenValue = jwtAuth.getToken().getTokenValue();
                        ClientRequest filteredRequest = ClientRequest.from(clientRequest)
                                .header("Authorization", "Bearer " + tokenValue)
                                .build();
                        return next.exchange(filteredRequest);
                    }
                    return next.exchange(clientRequest);
                });
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            System.out.println("WebClient Request: " + clientRequest.method() + " " + clientRequest.url());
            return next.exchange(clientRequest);
        };
    }
}

