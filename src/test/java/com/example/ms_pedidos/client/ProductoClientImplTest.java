package com.example.ms_pedidos.client;

import com.example.ms_pedidos.dto.ProductoDTO;
import org.junit.jupiter.api.BeforeEach; // Importa BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock; // <-- ¡YA NO HAY @InjectMocks!
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function; // Importa Function

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProductoClientImplTest {

    // --- ¡CAMBIO 1: Sin @InjectMocks! ---
    // Lo crearemos manualmente
    private ProductoClientImpl productoClient;

    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    // --- ¡CAMBIO 2: El setUp() HACE TODO! ---
    @BeforeEach
    void setUp() {
        // 1. Preparamos el "Builder"
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        // 2. AHORA SÍ, creamos manualmente el cliente
        // (El constructor se llama AHORA, y los mocks ya están listos)
        productoClient = new ProductoClientImpl(webClientBuilder);
    }


    @Test
    void testObtenerProducto_Success() {
        // --- 1. Preparación (Arrange) ---
        ProductoDTO mockProducto = new ProductoDTO();
        mockProducto.setId(1L);

        // --- Configuración de la secuencia de mocks (el webClient real) ---
        // (Tuve que añadir el .attributes() que vi en tu código)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyLong())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.attributes(any())).thenReturn(requestHeadersSpec); // <-- ¡Paso extra!
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ProductoDTO.class)).thenReturn(Mono.just(mockProducto));

        // --- 2. Ejecución (Act) ---
        Mono<ProductoDTO> resultadoMono = productoClient.obtenerProducto(1L);

        // --- 3. Verificación (Assert) ---
        StepVerifier.create(resultadoMono)
                .expectNextMatches(producto -> producto.getId().equals(1L))
                .verifyComplete();

        // Verificamos que se usó la URL correcta
        verify(requestHeadersUriSpec).uri(eq("/api/productos/{id}"), eq(1L));
    }
    @Test
    void testActualizarStock_Success() {
        // --- 1. Preparación (Arrange) ---
        long productoId = 1L;
        int cantidad = 5;

        // --- Configuración de la secuencia de mocks (el webClient real) ---
        // (Esta es la cadena corregida para PUT)

        // 1. this.webClient.put() -> devuelve un 'RequestBodyUriSpec'
        when(webClient.put()).thenReturn(requestBodyUriSpec);

        // 2. .uri(uriBuilder -> ...) -> devuelve un 'RequestBodySpec'
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);

        // 3. .attributes(...) -> devuelve un 'RequestHeadersSpec'
        //    (Aquí tu código tiene .attributes(), así que usamos un tipo más genérico)
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);

        // 4. .retrieve() -> devuelve un 'ResponseSpec'
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // 5. .bodyToMono(Void.class)
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // --- 2. Ejecución (Act) ---

        // Llamamos al método real
        Mono<Void> resultadoMono = productoClient.actualizarStock(productoId, cantidad);

        // --- 3. Verificación (Assert) ---

        // Para un Mono<Void>, solo verificamos que se complete
        StepVerifier.create(resultadoMono)
                .verifyComplete();

        // Verificamos que se llamó a toda la secuencia
        verify(webClient).put();
        verify(requestBodyUriSpec).uri(any(Function.class));
        verify(requestBodySpec).attributes(any());
        verify(requestBodySpec).retrieve();
    }
    @Test
    void testObtenerProducto_Error() {
        // --- 1. Preparación (Arrange) ---
        // Configura el mock para que devuelva un Mono de error
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyLong())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.attributes(any())).thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Aquí simulamos un error de red (la llamada falla con un RuntimeException)
        when(responseSpec.bodyToMono(ProductoDTO.class)).thenReturn(Mono.error(new RuntimeException("Error de red")));

        // --- 2. Ejecución (Act) ---
        Mono<ProductoDTO> resultadoMono = productoClient.obtenerProducto(2L);

        // --- 3. Verificación (Assert) ---
        StepVerifier.create(resultadoMono)
                // Esperamos que el Mono devuelva un error
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testActualizarStock_Error() {
        // --- 1. Preparación (Arrange) ---
        long productoId = 2L;
        int cantidad = 10;

        // --- Configuración de la secuencia de mocks ---
        when(webClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(any(Function.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.attributes(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        // Simula un error: el servidor responde con 4xx o 5xx
        when(responseSpec.bodyToMono(Void.class))
                .thenReturn(Mono.error(new RuntimeException("Error al actualizar el stock")));

        // --- 2. Ejecución (Act) ---
        Mono<Void> resultadoMono = productoClient.actualizarStock(productoId, cantidad);

        // --- 3. Verificación (Assert) ---
        StepVerifier.create(resultadoMono)
                // Esperamos que el Mono devuelva un error
                .expectError(RuntimeException.class)
                .verify();

        // Verificamos que se llamó a la secuencia
        verify(webClient).put();
        verify(requestBodyUriSpec).uri(any(Function.class));
        verify(webClient).put();
    }
}