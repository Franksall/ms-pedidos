package com.example.ms_pedidos.client;

import com.example.ms_pedidos.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component //  Spring  gestiona esta clase
public class ProductoClientImpl implements ProductoClient {

    private final WebClient webClient;

    // Inyectamos el WebClient.Builder de MsPedidosApplication
    @Autowired
    public ProductoClientImpl(WebClient.Builder webClientBuilder) {
        //  Apunta al nombre de Eureka (lb = Load Balanced)
        this.webClient = webClientBuilder
                .baseUrl("lb://ms-productos")
                .build();
    }

    @Override
    public Mono<ProductoDTO> obtenerProducto(Long id) {
        // Llama a GET http://localhost:8081/api/productos/{id}
        return this.webClient.get()
                .uri("/api/productos/{id}", id)
                .attributes(clientRegistrationId("gateway-client-registration"))
                .retrieve()
                .bodyToMono(ProductoDTO.class);
    }

    @Override
    public Mono<Void> actualizarStock(Long id, Integer cantidad) {
        // Llama a PUT http://localhost:8081/api/productos/{id}/stock?cantidad={cantidad}
        return this.webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/productos/{id}/stock")
                        .queryParam("cantidad", cantidad)
                        .build(id))
                .attributes(clientRegistrationId("gateway-client-registration"))
                .retrieve()
                .bodyToMono(Void.class);
    }
}