package com.example.ms_pedidos.service;

// Imports de tus clases
import com.example.ms_pedidos.model.Pedido;
import com.example.ms_pedidos.model.DetallePedido; // ¡Importante!
import com.example.ms_pedidos.repository.DetallePedidoRepository;
import com.example.ms_pedidos.repository.PedidoRepository;
import com.example.ms_pedidos.client.ProductoClient;

// Imports de Pruebas
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Imports Reactivos
import com.example.ms_pedidos.dto.ProductoDTO;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

// Imports estáticos
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private ProductoClient productoClient;

    @Mock
    private ReactiveCircuitBreakerFactory cbFactory;

    @Mock
    private ReactiveCircuitBreaker cb; // Un mock "dummy" del circuit breaker

    @InjectMocks
    private PedidoServiceImpl pedidoService;


    @Test
    void testFindAll() { // <-- 1. Nombre corregido
        // --- 1. Preparación (Arrange) ---

        // Creamos un pedido mock
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setEstado("CREADO");
        // (En este punto, pedido1.getDetalles() está vacío)

        // Creamos un detalle mock
        DetallePedido detalle1 = new DetallePedido();
        detalle1.setId(10L);
        detalle1.setPedidoId(1L);
        detalle1.setProductoId(99L);

        // Mock 1: "Cuando llamen a pedidoRepository.findAll(), devuelve el pedido1"
        when(pedidoRepository.findAll()).thenReturn(Flux.just(pedido1));

        // Mock 2: "Cuando llamen a detalleRepo.findByPedidoId(1L) (desde loadDetalles),
        //          devuelve el detalle1"
        when(detallePedidoRepository.findByPedidoId(1L)).thenReturn(Flux.just(detalle1));

        // --- 2. Ejecución (Act) ---

        // Llamamos al método real del servicio (nombre corregido)
        Flux<Pedido> resultadoFlux = pedidoService.findAll();

        // --- 3. Verificación (Assert) ---

        // Usamos StepVerifier para probar el Flux
        StepVerifier.create(resultadoFlux)
                // Esperamos que emita un elemento (nuestro pedido1)
                .expectNextMatches(pedido -> {
                    // Verificamos que sea el pedido correcto
                    boolean idMatches = pedido.getId().equals(1L);

                    // ¡Verificamos que loadDetalles() funcionó!
                    // El pedido ahora debe contener el detalle
                    boolean detallesMatch = pedido.getDetalles() != null &&
                            pedido.getDetalles().size() == 1 &&
                            pedido.getDetalles().get(0).getId().equals(10L);

                    return idMatches && detallesMatch;
                })
                // Esperamos que no emita más elementos y se complete
                .verifyComplete();

        // Verificación de Mocks:
        // Verificamos que SÍ se llamó a ambos repositorios
        verify(pedidoRepository, times(1)).findAll();
        verify(detallePedidoRepository, times(1)).findByPedidoId(1L);
        // Verificamos que NUNCA se usó el cliente de productos
        verifyNoInteractions(productoClient);
    }
    @Test
    void testFindById_Success() {
        // --- 1. Preparación (Arrange) ---

        // Creamos un pedido mock
        Pedido pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setEstado("PENDIENTE");

        // Creamos un detalle mock
        DetallePedido detalle1 = new DetallePedido();
        detalle1.setId(10L);
        detalle1.setPedidoId(1L);

        // Mock 1: "Cuando llamen a pedidoRepo.findById(1L), devuelve el pedido1"
        when(pedidoRepository.findById(1L)).thenReturn(Mono.just(pedido1));

        // Mock 2: "Cuando llamen a detalleRepo.findByPedidoId(1L), devuelve el detalle1"
        when(detallePedidoRepository.findByPedidoId(1L)).thenReturn(Flux.just(detalle1));

        // --- 2. Ejecución (Act) ---

        // Llamamos al método real del servicio
        Mono<Pedido> resultadoMono = pedidoService.findById(1L);

        // --- 3. Verificación (Assert) ---

        // Verificamos que el Mono emita el pedido correcto
        StepVerifier.create(resultadoMono)
                .expectNextMatches(pedido -> {
                    boolean idMatches = pedido.getId().equals(1L);
                    // Verificamos que los detalles se cargaron
                    boolean detallesMatch = pedido.getDetalles() != null &&
                            pedido.getDetalles().size() == 1;
                    return idMatches && detallesMatch;
                })
                .verifyComplete();

        // Verificamos que se llamó a los mocks
        verify(pedidoRepository, times(1)).findById(1L);
        verify(detallePedidoRepository, times(1)).findByPedidoId(1L);
    }


    // --- AÑADE ESTA OTRA PRUEBA (Camino Triste: No Encontrado) ---
    @Test
    void testFindById_NotFound() {
        // --- 1. Preparación (Arrange) ---

        // ID que no existe
        long idNoExistente = 99L;

        // Mock: "Cuando llamen a pedidoRepo.findById(99L), devuelve un Mono vacío"
        when(pedidoRepository.findById(idNoExistente)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---

        // Llamamos al método real del servicio
        Mono<Pedido> resultadoMono = pedidoService.findById(idNoExistente);

        // --- 3. Verificación (Assert) ---

        // Verificamos que el Mono se complete SIN emitir ningún pedido
        StepVerifier.create(resultadoMono)
                .expectNextCount(0) // Esperamos cero elementos
                .verifyComplete();

        // Verificamos que SÓLO se llamó al pedidoRepository
        verify(pedidoRepository, times(1)).findById(idNoExistente);
        // Verificamos que NUNCA se llamó a detalleRepository (porque no se encontró pedido)
        verify(detallePedidoRepository, never()).findByPedidoId(anyLong());
    }
    @Test
    void testCreatePedido_Success() {
        // --- 1. Preparación (Arrange) ---

        // 1a. Datos de entrada
        DetallePedido detalleInput = new DetallePedido();
        detalleInput.setProductoId(1L);
        detalleInput.setCantidad(2);

        Pedido pedidoInput = new Pedido();
        pedidoInput.setDetalles(List.of(detalleInput));

        // 1b. Mocks de lo que devuelven las dependencias
        ProductoDTO mockProducto = new ProductoDTO();
        mockProducto.setId(1L);
        mockProducto.setNombre("Teclado");
        mockProducto.setPrecio(100.0);
        mockProducto.setStock(10); // ¡Hay stock suficiente!

        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(123L); // ID generado por la BD
        pedidoGuardado.setEstado("PENDIENTE");
        pedidoGuardado.setFecha(LocalDateTime.now());
        pedidoGuardado.setTotal(200.0); // Total calculado
        pedidoGuardado.setDetalles(pedidoInput.getDetalles());

        DetallePedido detalleGuardado = new DetallePedido();
        detalleGuardado.setPedidoId(123L); // ID del pedido padre
        detalleGuardado.setProductoId(1L);
        detalleGuardado.setCantidad(2);
        detalleGuardado.setPrecioUnitario(100.0); // Precio asignado

        // 1c. Configuración de Mocks

        // Mock del CircuitBreaker
        when(cbFactory.create(anyString())).thenReturn(cb);

        // Mock 1: "Cuando el cliente busque el producto 1L, devuelve el mockProducto"
        when(productoClient.obtenerProducto(1L)).thenReturn(Mono.just(mockProducto));

        // Mock 2: "Cuando se guarde el pedido, devuelve el pedidoGuardado (con ID)"
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(Mono.just(pedidoGuardado));

        // Mock 3: "Cuando se guarde el detalle, devuelve el detalleGuardado"
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenReturn(Mono.just(detalleGuardado));

        // Mock 4: "Cuando se actualice el stock, devuelve Mono.empty()"
        when(productoClient.actualizarStock(1L, 2)).thenReturn(Mono.empty());


        // --- 2. Ejecución (Act) ---

        // Llamamos al método real del servicio
        Mono<Pedido> resultadoMono = pedidoService.createPedido(pedidoInput);

        // --- 3. Verificación (Assert) ---

        StepVerifier.create(resultadoMono)
                .expectNextMatches(pedidoFinal -> {
                    // Verificamos que el pedido devuelto sea correcto
                    boolean idOk = pedidoFinal.getId().equals(123L);
                    boolean totalOk = pedidoFinal.getTotal() == 200.0;
                    boolean estadoOk = pedidoFinal.getEstado().equals("PENDIENTE");

                    boolean detalleOk = pedidoFinal.getDetalles() != null &&
                            pedidoFinal.getDetalles().size() == 1 &&
                            pedidoFinal.getDetalles().get(0).getPrecioUnitario() == 100.0;

                    return idOk && totalOk && estadoOk && detalleOk;
                })
                .verifyComplete();

        // Verificamos que todos los mocks fueron llamados
        verify(productoClient, times(1)).obtenerProducto(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(detallePedidoRepository, times(1)).save(any(DetallePedido.class));
        verify(productoClient, times(1)).actualizarStock(1L, 2);
        verify(cbFactory, times(1)).create("productos-cb");
    }
    @Test
    void testCreatePedido_Error_StockInsuficiente() {
        // --- 1. Preparación (Arrange) ---

        // 1a. Datos de entrada (pide 10)
        DetallePedido detalleInput = new DetallePedido();
        detalleInput.setProductoId(1L);
        detalleInput.setCantidad(10); // Pide 10

        Pedido pedidoInput = new Pedido();
        pedidoInput.setDetalles(List.of(detalleInput));

        // 1b. Mock del ProductoDTO (¡Solo tiene 5!)
        ProductoDTO mockProducto = new ProductoDTO();
        mockProducto.setId(1L);
        mockProducto.setNombre("Mouse");
        mockProducto.setPrecio(50.0);
        mockProducto.setStock(5); // <-- ¡Stock insuficiente!

        // 1c. Configuración de Mocks

        // Mock del CircuitBreaker
        when(cbFactory.create(anyString())).thenReturn(cb);

        // Mock: "Cuando el cliente busque el producto 1L, devuelve el mockProducto"
        when(productoClient.obtenerProducto(1L)).thenReturn(Mono.just(mockProducto));


        // --- 2. Ejecución (Act) ---

        // Llamamos al método real del servicio
        Mono<Pedido> resultadoMono = pedidoService.createPedido(pedidoInput);

        // --- 3. Verificación (Assert) ---

        // Verificamos que el Mono devuelva un ERROR
        StepVerifier.create(resultadoMono)
                // Usamos .expectErrorMatches() para verificar TIPO y MENSAJE en un solo paso
                .expectErrorMatches(error ->
                        // 1. Verificamos que sea un RuntimeException
                        error instanceof RuntimeException &&
                                // 2. Verificamos que el mensaje contenga el texto
                                error.getMessage().contains("Stock insuficiente")
                )
                .verify();

        // Verificamos qué mocks se usaron y cuáles NO
        verify(productoClient, times(1)).obtenerProducto(1L);

        // ¡Verificamos que NUNCA se intentó guardar nada!
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
        verify(productoClient, never()).actualizarStock(anyLong(), anyInt());
    }
    @Test
    void testUpdateEstado_Success() {
        // --- 1. Preparación (Arrange) ---
        long pedidoId = 1L;
        String nuevoEstado = "ENVIADO";

        // Mock del pedido que encontramos en la BD
        Pedido pedidoMock = new Pedido();
        pedidoMock.setId(pedidoId);
        pedidoMock.setEstado("PENDIENTE"); // Estado antiguo

        // Mock de un detalle (para probar el loadDetalles)
        DetallePedido detalleMock = new DetallePedido();
        detalleMock.setPedidoId(pedidoId);

        // Mock 1: "Cuando busquen el pedido 1L, devuélvelo"
        when(pedidoRepository.findById(pedidoId)).thenReturn(Mono.just(pedidoMock));

        // Mock 2: "Cuando guarden el pedido (ya actualizado), devuélvelo"
        // Mockito es lo bastante inteligente para devolver el mismo objeto 'pedidoMock'
        // que ya tendrá el estado "ENVIADO"
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(Mono.just(pedidoMock));

        // Mock 3: "Cuando llame a loadDetalles, devuelve el detalle"
        when(detallePedidoRepository.findByPedidoId(pedidoId)).thenReturn(Flux.just(detalleMock));

        // --- 2. Ejecución (Act) ---
        Mono<Pedido> resultadoMono = pedidoService.updateEstado(pedidoId, nuevoEstado);

        // --- 3. Verificación (Assert) ---
        StepVerifier.create(resultadoMono)
                .expectNextMatches(pedidoGuardado -> {
                    // Verificamos que el estado se actualizó
                    boolean estadoOk = pedidoGuardado.getEstado().equals("ENVIADO");
                    // Verificamos que los detalles se cargaron
                    boolean detallesOk = pedidoGuardado.getDetalles() != null &&
                            pedidoGuardado.getDetalles().size() == 1;
                    return estadoOk && detallesOk;
                })
                .verifyComplete();

        // Verificamos que se llamó a todos los mocks
        verify(pedidoRepository, times(1)).findById(pedidoId);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(detallePedidoRepository, times(1)).findByPedidoId(pedidoId);
    }

    // --- AÑADE ESTA PRUEBA (Camino Triste: No Encontrado) ---
    @Test
    void testUpdateEstado_NotFound() {
        // --- 1. Preparación (Arrange) ---
        long idNoExistente = 99L;
        String nuevoEstado = "ENVIADO";

        // Mock: "Cuando busquen el pedido 99L, devuelve vacío"
        when(pedidoRepository.findById(idNoExistente)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---
        Mono<Pedido> resultadoMono = pedidoService.updateEstado(idNoExistente, nuevoEstado);

        // --- 3. Verificación (Assert) ---
        // Verificamos que no emita nada y se complete
        StepVerifier.create(resultadoMono)
                .expectNextCount(0)
                .verifyComplete();

        // Verificamos que SÓLO se llamó a findById
        verify(pedidoRepository, times(1)).findById(idNoExistente);
        // Verificamos que NUNCA se intentó guardar ni cargar detalles
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(detallePedidoRepository, never()).findByPedidoId(anyLong());
    }
    @Test
    void testDeletePedido_Success() {
        // --- 1. Preparación (Arrange) ---
        long pedidoId = 1L;

        // Mock de un detalle que encontramos para borrar
        DetallePedido detalleMock = new DetallePedido();
        detalleMock.setId(10L);
        detalleMock.setPedidoId(pedidoId);

        // Mock 1: "Cuando busquen detalles para el pedidoId, devuelve uno"
        when(detallePedidoRepository.findByPedidoId(pedidoId)).thenReturn(Flux.just(detalleMock));

        // Mock 2: "Cuando borren ese detalle, devuelve Mono.empty()"
        when(detallePedidoRepository.delete(detalleMock)).thenReturn(Mono.empty());

        // Mock 3: "Cuando borren el pedido principal, devuelve Mono.empty()"
        when(pedidoRepository.deleteById(pedidoId)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---
        Mono<Void> resultadoMono = pedidoService.deletePedido(pedidoId);

        // --- 3. Verificación (Assert) ---
        StepVerifier.create(resultadoMono)
                // Verificamos que se complete sin emitir nada (es un Mono<Void>)
                .verifyComplete();

        // Verificamos que se llamó a toda la secuencia de borrado
        verify(detallePedidoRepository, times(1)).findByPedidoId(pedidoId);
        verify(detallePedidoRepository, times(1)).delete(detalleMock);
        verify(pedidoRepository, times(1)).deleteById(pedidoId);
    }
    @Test
    void testCreatePedido_Error_ProductoNoEncontrado() {
        // --- 1. Preparación (Arrange) ---

        // 1a. Datos de entrada (pide un producto con ID 999)
        DetallePedido detalleInput = new DetallePedido();
        detalleInput.setProductoId(999L); // ID que no existe
        detalleInput.setCantidad(1);

        Pedido pedidoInput = new Pedido();
        pedidoInput.setDetalles(List.of(detalleInput));

        // 1b. Configuración de Mocks

        // Mock del CircuitBreaker
        when(cbFactory.create(anyString())).thenReturn(cb);

        // Mock: "Cuando el cliente busque el producto 999L, devuelve Mono.empty()"
        when(productoClient.obtenerProducto(999L)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---
        Mono<Pedido> resultadoMono = pedidoService.createPedido(pedidoInput);

        // --- 3. Verificación (Assert) ---

        // Verificamos que el Mono devuelva un ERROR
        StepVerifier.create(resultadoMono)
                // Verificamos que sea un RuntimeException y que el mensaje coincida
                .expectErrorMatches(error ->
                        error instanceof RuntimeException &&
                                error.getMessage().contains("Producto no encontrado: 999")
                )
                .verify();

        // Verificamos qué mocks se usaron y cuáles NO
        verify(productoClient, times(1)).obtenerProducto(999L);

        // ¡Verificamos que NUNCA se intentó guardar nada!
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(detallePedidoRepository, never()).save(any(DetallePedido.class));
        verify(productoClient, never()).actualizarStock(anyLong(), anyInt());
    }

    // ¡Aquí añadiremos más @Test para los otros métodos (findById, createPedido, etc.)!
}