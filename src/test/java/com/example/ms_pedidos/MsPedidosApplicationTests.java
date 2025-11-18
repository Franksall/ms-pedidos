package com.example.ms_pedidos;

import com.example.ms_pedidos.client.ProductoClient;
import com.example.ms_pedidos.repository.DetallePedidoRepository;
import com.example.ms_pedidos.repository.PedidoRepository;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class MsPedidosApplicationTests {

    @MockBean
    private PedidoRepository pedidoRepository;

    @MockBean
    private DetallePedidoRepository detallePedidoRepository;

    @MockBean
    private ProductoClient productoClient;

    // ESTE ES EL MOCK QUE TE FALTABA
    @MockBean
    private org.springframework.web.reactive.function.client.WebClient.Builder webClientBuilder;

    @Test
    void contextLoads() {
        // Si llega aquí → el contexto arrancó sin fallar
    }
}
