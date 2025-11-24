package com.example.ms_pedidos.client;
import org.springframework.beans.factory.annotation.Qualifier;
import com.example.ms_pedidos.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


import java.time.Duration;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class ProductoClientImpl implements ProductoClient {

    private final WebClient webClient;

    @Autowired
    public ProductoClientImpl(@Qualifier("plainWebClientBuilder") WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://ms-productos:8081/")
                .build();
    }

    @Override
    public Mono<ProductoDTO> obtenerProducto(Long id) {
        return this.webClient.get()
                .uri("/api/productos/{id}", id)
                .retrieve()
                .bodyToMono(ProductoDTO.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));
    }

    @Override
    public Mono<Void> actualizarStock(Long id, Integer cantidad) {
        return this.webClient.put()
                .uri("/api/productos/{id}/stock?cantidad={cantidad}", id, cantidad)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)));
    }
}
