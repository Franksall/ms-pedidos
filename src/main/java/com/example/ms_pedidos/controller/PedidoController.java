package com.example.ms_pedidos.controller;

import com.example.ms_pedidos.model.Pedido;
import com.example.ms_pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pedidos") // URL base para todos los endpoints
public class PedidoController {

    @Autowired
    private PedidoService service;

    // GET /api/pedidos
    @GetMapping
    public Flux<Pedido> getAllPedidos() {
        return service.findAll();
    }

    // GET /api/pedidos/{id}
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Pedido>> getPedidoById(@PathVariable Long id) {
        return service.findById(id)
                .map(pedido -> ResponseEntity.ok(pedido)) // Devuelve 200 OK si lo encuentra
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Devuelve 404 si no
    }

    // POST /api/pedidos (crear pedido)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // Devuelve un código 201 Created
    public Mono<Pedido> createPedido(@RequestBody Pedido pedido) {
        // La lógica de validación de stock está en el service
        return service.createPedido(pedido);
    }

    // PUT /api/pedidos/{id}/estado
    @PutMapping("/{id}/estado")
    public Mono<ResponseEntity<Pedido>> updateEstadoPedido(
            @PathVariable Long id,
            @RequestParam String estado) { // Recibe el estado como parámetro (estado=PROCESADO)

        return service.updateEstado(id, estado)
                .map(pedido -> ResponseEntity.ok(pedido))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // DELETE /api/pedidos/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Devuelve un código 204 No Content
    public Mono<Void> deletePedido(@PathVariable Long id) {
        return service.deletePedido(id);
    }
}