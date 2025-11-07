package com.example.ms_pedidos.repository;

import com.example.ms_pedidos.model.DetallePedido;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DetallePedidoRepository extends R2dbcRepository<DetallePedido, Long> {

    // Como no hay @OneToMany, creamos este metodo para buscar todos
    // los detalles que pertenezcan a un 'pedidoId' específico.
    Flux<DetallePedido> findByPedidoId(Long pedidoId);
}