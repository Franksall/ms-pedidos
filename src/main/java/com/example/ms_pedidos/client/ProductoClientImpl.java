package com.example.ms_pedidos.client;

import com.example.ms_pedidos.dto.ProductoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component //  Spring  gestiona esta clase
public class ProductoClientImpl implements ProductoClient {

    private final WebClient webClient;

    // Inyectamos el WebClient.Builder de MsPedidosApplication
    @Autowired
    public ProductoClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<ProductoDTO> obtenerProducto(Long id) {
        // Llama a GET http://localhost:8081/api/productos/{id}
        return this.webClient.get()
                .uri("/api/productos/{id}", id)
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
                .retrieve()
                .bodyToMono(Void.class);
    }
}