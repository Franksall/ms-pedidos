package com.example.ms_pedidos.client;

import com.example.ms_pedidos.dto.ProductoDTO;
import reactor.core.publisher.Mono;

// Versión reactiva de la Tarea 3.4
public interface ProductoClient {

    Mono<ProductoDTO> obtenerProducto(Long id);

    Mono<Void> actualizarStock(Long id, Integer cantidad);
}