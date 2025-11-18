package com.example.ms_pedidos.service;

import com.example.ms_pedidos.client.ProductoClient;
import com.example.ms_pedidos.model.Pedido;
import com.example.ms_pedidos.model.DetallePedido;
import com.example.ms_pedidos.repository.DetallePedidoRepository;
import com.example.ms_pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    @Autowired
    private ProductoClient productoClient; // Nuestro cliente reactivo


    // CircuitBreaker
    @Autowired
    private ReactiveCircuitBreakerFactory cbFactory;

    @Override
    public Flux<Pedido> findAll() {
        // Busca  los pedidos y para cada uno, carga sus detalles
        return pedidoRepository.findAll()
                .flatMap(this::loadDetalles);
    }

    @Override
    public Mono<Pedido> findById(Long id) {
        // Busca un pedido por ID y carga sus detalles
        return pedidoRepository.findById(id)
                .flatMap(this::loadDetalles);
    }

    /**
     * Tarea 3.5 Lógica de negocio para crear un pedido
     *  Validar Stock de cada producto
     *  Calcular Total
     *  Crear Pedido y Detalles
     *  Actualizar Stock en ms-productos
     */
    @Override
    @Transactional // Para asegurar que todo se guarde (o nada lo haga)
    public Mono<Pedido> createPedido(Pedido pedido) {
        // 1. Validar productos y calcular precios
        Mono<Pedido> pedidoValidado = Flux.fromIterable(pedido.getDetalles())
                .flatMap(detalle -> {
                    // Llama a ms-productos para obtener info del producto
                    // 1. Creamos el cortacircuitos usando el nombre del .yml
                    ReactiveCircuitBreaker cb = cbFactory.create("productos-cb");
                    return productoClient.obtenerProducto(detalle.getProductoId())
                            .switchIfEmpty(Mono.error(new RuntimeException("Producto no encontrado: " + detalle.getProductoId())))
                            .flatMap(productoDTO -> {
                                // Tarea 3.5: Validar disponibilidad
                                if (productoDTO.getStock() < detalle.getCantidad()) {
                                    return Mono.error(new RuntimeException("Stock insuficiente para: " + productoDTO.getNombre()));
                                }
                                // Tarea 3.5: Usamos el precio de la BD, no el del cliente
                                detalle.setPrecioUnitario(productoDTO.getPrecio());
                                return Mono.just(detalle);
                            });
                })
                .collectList() // Vuelve a juntar todos los detalles validados
                .flatMap(detallesValidados -> {
                    // Tarea 3.5: Calcular totales
                    double total = detallesValidados.stream()
                            .mapToDouble(d -> d.getPrecioUnitario() * d.getCantidad())
                            .sum();

                    // Asignar valores al pedido
                    pedido.setTotal(total);
                    pedido.setDetalles(detallesValidados); // Asigna los detalles validados
                    pedido.setFecha(LocalDateTime.now());
                    pedido.setEstado("PENDIENTE"); // Estado inicial
                    return Mono.just(pedido);
                });

        // 2. Guardar Pedido y Detalles, y  Actualizar Stock
        return pedidoValidado
                .flatMap(p -> pedidoRepository.save(p)) // Guarda el Pedido para obtener un ID
                .flatMap(savedPedido ->
                        // Asigna nuevo ID del pedido a cada detalle
                        Flux.fromIterable(savedPedido.getDetalles())
                                .flatMap(detalle -> {
                                    detalle.setPedidoId(savedPedido.getId());
                                    return detallePedidoRepository.save(detalle); // Guarda cada detalle
                                })
                                .collectList() // Junta los detalles guardados
                                .map(savedDetalles -> {
                                    savedPedido.setDetalles(savedDetalles); // Asigna los detalles finales al pedido
                                    return savedPedido;
                                })
                )
                .flatMap(savedPedidoConDetalles -> {
                    // Tarea 3.5: Actualizar stock de productos
                    // (Llama al procedimiento almacenado de ms-productos)
                    return Flux.fromIterable(savedPedidoConDetalles.getDetalles())
                            .flatMap(detalle ->
                                    // Llama a: PUT /api/productos/{id}/stock?cantidad={cantidad}
                                    productoClient.actualizarStock(detalle.getProductoId(), detalle.getCantidad())
                            )
                            .then(Mono.just(savedPedidoConDetalles)); // Devuelve el pedido completo
                });
    }

    @Override
    public Mono<Pedido> updateEstado(Long id, String estado) {
        return pedidoRepository.findById(id)
                .flatMap(pedido -> {
                    pedido.setEstado(estado);
                    return pedidoRepository.save(pedido);
                })
                .flatMap(this::loadDetalles); // Recarga los detalles para la respuesta
    }

    @Override
    public Mono<Void> deletePedido(Long id) {
        // (En R2DBC no hay cascade delete automático)
        // Primero borra los detalles, luego el pedido
        return detallePedidoRepository.findByPedidoId(id)
                .flatMap(detalle -> detallePedidoRepository.delete(detalle))
                .then(pedidoRepository.deleteById(id));
    }

    /**
     * Metodo helper para cargar los detalles de un pedido,
     * ya que @Transient no los carga automaticamente.
     */
    private Mono<Pedido> loadDetalles(Pedido pedido) {
        return detallePedidoRepository.findByPedidoId(pedido.getId())
                .collectList()
                .map(detalles -> {
                    pedido.setDetalles(detalles);
                    return pedido;
                });
    }
}