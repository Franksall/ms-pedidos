package com.example.ms_pedidos.repository;

import com.example.ms_pedidos.model.Pedido;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends R2dbcRepository<Pedido, Long> {
}