package com.example.ms_pedidos.service;

import com.example.ms_pedidos.model.Pedido;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Define la lógica de negocio para el ms-pedidos
 *  Basado en la parte ded Tarea 3.6
 */
public interface PedidoService {

    // GET /api/pedidos
    Flux<Pedido> findAll();

    // GET /api/pedidos/{id}
    Mono<Pedido> findById(Long id);

    // POST /api/pedidos (crear pedido)
    Mono<Pedido> createPedido(Pedido pedido);

    // PUT /api/pedidos/{id}/estado
    Mono<Pedido> updateEstado(Long id, String estado);

    // DELETE /api/pedidos/{id}
    Mono<Void> deletePedido(Long id);
}