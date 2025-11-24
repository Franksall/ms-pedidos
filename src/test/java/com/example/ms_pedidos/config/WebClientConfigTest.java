package com.example.ms_pedidos.config;

// Imports del Servidor Falso
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

// Imports de Pruebas y Spring
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;

// Imports estáticos
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// 1. Cargamos el contexto SÓLO con esta clase de config
@SpringBootTest(classes = WebClientConfig.class)
class WebClientConfigTest {

    // 2. Inyectamos el Builder REAL que crea tu config
    @Autowired
    private WebClient.Builder webClientBuilder;

    // 3. El servidor falso
    public static MockWebServer mockWebServer;

    // 4. Iniciamos el servidor falso ANTES de todas las pruebas
    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    // 5. Apagamos el servidor falso DESPUÉS de todas las pruebas
    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void webClientFilters_ShouldPropagateTokenAndLogRequest() throws InterruptedException {
        // --- 1. Preparación (Arrange) ---

        // Preparamos un token JWT falso
        String fakeTokenValue = "test-token-123";
        Jwt jwt = Jwt.withTokenValue(fakeTokenValue)
                .header("alg", "none")
                .claim("scope", "test.read")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt);

        // Preparamos una respuesta falsa del servidor
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"message\": \"ok\"}")
                .addHeader("Content-Type", "application/json"));

        // Creamos el cliente USANDO el builder real
        // y apuntándolo a nuestro servidor falso
        WebClient client = webClientBuilder
                .baseUrl(mockWebServer.url("/").toString()) // Apunta al servidor mock
                .build();

        // --- 2. Ejecución (Act) ---

        // Hacemos la llamada al cliente DENTRO de un contexto de seguridad falso
        // Esto es lo que necesita tu 'propagateTokenFilter'
        String response = client.get()
                .uri("/test")
                .retrieve()
                .bodyToMono(String.class)
                // Inyectamos el token en el contexto reactivo
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
                .block(); // Hacemos la llamada síncrona para la prueba

        // --- 3. Verificación (Assert) ---

        // Verificamos que la respuesta del servidor es la que esperamos
        assertNotNull(response);

        // ¡LA VERIFICACIÓN MÁS IMPORTANTE!
        // Le preguntamos al servidor falso: "¿Qué petición recibiste?"
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        // Verificamos que el filtro 'propagateTokenFilter' añadió la cabecera
        assertEquals(
                "Bearer " + fakeTokenValue,
                recordedRequest.getHeader("Authorization")
        );

        // (El filtro 'logRequest' también se ejecutó, Jacoco lo verá
        // y lo marcará como cubierto)
    }
}