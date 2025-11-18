package com.example.ms_pedidos.controller;

import com.example.ms_pedidos.SecurityConfig;
import com.example.ms_pedidos.model.Pedido;
import com.example.ms_pedidos.service.PedidoService;

// Imports de Prueba Web
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Flux;


// Imports para la seguridad de la prueba
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;


// Imports estáticos
import static org.mockito.Mockito.when; // Importamos solo los que usamos
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

// ¡DEJAMOS EL @WebFluxTest LIMPIO!
@WebFluxTest(controllers = PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

    @Autowired
    private WebTestClient webClient;

    // 1. Mockeamos el servicio QUE USA EL CONTROLADOR
    @MockBean
    private PedidoService pedidoService;

    // --- ¡CAMBIOS AQUÍ (PARTE 2)! ---
    // 2. Mockeamos el Bean que "SecurityConfig" necesita para arrancar
    @MockBean
    private ReactiveJwtDecoder jwtDecoder;

    // (¡Quitamos todos los otros @MockBean de repositorios!
    // Esas son para la prueba unitaria, no para esta)


    @Test
    void testGetPedidos_Success() {
        // --- 1. Preparación (Arrange) ---
        Pedido pedidoMock = new Pedido();
        pedidoMock.setId(1L);
        pedidoMock.setEstado("ENVIADO");

        when(pedidoService.findAll()).thenReturn(Flux.just(pedidoMock));

        // --- 2. Ejecución (Act) ---

        // --- ¡CAMBIOS AQUÍ (PARTE 3)! ---
        // Le decimos al cliente que "mute" la petición
        // y le inyecte un token JWT falso
        webClient.mutateWith(mockJwt()) // <-- ¡ESTO SATISFACE LA SEGURIDAD!
                .get().uri("/api/pedidos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // --- 3. Verificación (Assert) ---
                .expectStatus().isOk() // Ahora SÍ esperamos un 200 OK
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].estado").isEqualTo("ENVIADO");

        verify(pedidoService, times(1)).findAll();
    }
    @Test
    void testGetPedidoById_Success() {
        // --- 1. Preparación (Arrange) ---
        long pedidoId = 1L;
        Pedido pedidoMock = new Pedido();
        pedidoMock.setId(pedidoId);
        pedidoMock.setEstado("ENVIADO");
        pedidoMock.setTotal(200.0);

        // Mock: "Cuando el servicio busque por ID 1, devuelve el pedidoMock"
        when(pedidoService.findById(pedidoId)).thenReturn(Mono.just(pedidoMock));

        // --- 2. Ejecución (Act) ---
        webClient.mutateWith(mockJwt()) // <-- ¡Acuérdate de la seguridad!
                .get().uri("/api/pedidos/{id}", pedidoId) // <-- Usamos la URI con el ID
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // --- 3. Verificación (Assert) ---
                .expectStatus().isOk() // Esperamos un 200 OK
                .expectBody()
                .jsonPath("$.id").isEqualTo(pedidoId)
                .jsonPath("$.estado").isEqualTo("ENVIADO");

        // Verificamos que se llamó al servicio
        verify(pedidoService, times(1)).findById(pedidoId);
    }

    // --- AÑADE ESTA PRUEBA (Camino Triste: GET /api/pedidos/{id} No Encontrado) ---
    @Test
    void testGetPedidoById_NotFound() {
        // --- 1. Preparación (Arrange) ---
        long idNoExistente = 99L;

        // Mock: "Cuando el servicio busque por ID 99, devuelve vacío"
        when(pedidoService.findById(idNoExistente)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---
        webClient.mutateWith(mockJwt()) // <-- ¡Seguridad!
                .get().uri("/api/pedidos/{id}", idNoExistente) // <-- ID que no existe
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // --- 3. Verificación (Assert) ---
                .expectStatus().isNotFound(); // <-- ¡Esperamos un 404 Not Found!

        // Verificamos que se llamó al servicio
        verify(pedidoService, times(1)).findById(idNoExistente);
    }
    @Test
    void testCreatePedido_Success() {

        Pedido pedidoInput = new Pedido();
        pedidoInput.setEstado("PENDIENTE");

        Pedido pedidoMock = new Pedido();
        pedidoMock.setId(1L);
        pedidoMock.setEstado("PENDIENTE");

        when(pedidoService.createPedido(any(Pedido.class)))
                .thenReturn(Mono.just(pedidoMock));

        webClient.mutateWith(
                        mockJwt()
                                .jwt(jwt -> jwt.claim("scope", "pedido.write"))
                                .authorities(new SimpleGrantedAuthority("SCOPE_pedido.write"))
                )
                .post().uri("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pedidoInput)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Pedido.class)
                .value(p -> assertEquals("PENDIENTE", p.getEstado()));

        verify(pedidoService, times(1)).createPedido(any(Pedido.class));
    }
    @Test
    void testUpdateEstadoPedido_Success() {
        // --- 1. Preparación (Arrange) ---
        long pedidoId = 1L;
        String nuevoEstado = "ENVIADO";

        Pedido pedidoMock = new Pedido();
        pedidoMock.setId(pedidoId);
        pedidoMock.setEstado(nuevoEstado); // El estado ya actualizado
        pedidoMock.setTotal(200.0);

        // Mock: "Cuando el servicio actualice, devuelve el pedido actualizado"
        when(pedidoService.updateEstado(pedidoId, nuevoEstado)).thenReturn(Mono.just(pedidoMock));

        // --- 2. Ejecución (Act) ---
        webClient.mutateWith(
                        // Asumimos que "pedido.write" también cubre actualizar
                        mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_pedido.write"))
                )
                .put().uri("/api/pedidos/{id}/estado?estado={estado}", pedidoId, nuevoEstado) // <-- ¡PUT!
                .accept(MediaType.APPLICATION_JSON)
                .exchange()

                // --- 3. Verificación (Assert) ---
                .expectStatus().isOk() // Esperamos un 200 OK
                .expectBody()
                .jsonPath("$.id").isEqualTo(pedidoId)
                .jsonPath("$.estado").isEqualTo(nuevoEstado);

        // Verificamos que se llamó al servicio
        verify(pedidoService, times(1)).updateEstado(pedidoId, nuevoEstado);
    }
    @Test
    void testDeletePedido_Success() {
        // --- 1. Preparación (Arrange) ---
        long pedidoId = 1L;

        // Mock: "Cuando el servicio borre el pedido, devuelve Mono.empty()"
        when(pedidoService.deletePedido(pedidoId)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---
        webClient.mutateWith(
                        // Asumimos que "pedido.write" también cubre borrar
                        mockJwt().authorities(new SimpleGrantedAuthority("SCOPE_pedido.write"))
                )
                .delete().uri("/api/pedidos/{id}", pedidoId) // <-- ¡DELETE!
                .exchange()

                // --- 3. Verificación (Assert) ---
                // Tu controlador tiene @ResponseStatus(HttpStatus.NO_CONTENT)
                .expectStatus().isNoContent(); // <-- ¡Esperamos un 204 No Content!

        // Verificamos que se llamó al servicio
        verify(pedidoService, times(1)).deletePedido(pedidoId);
    }
}