package com.example.ms_pedidos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication

public class MsPedidosApplication {

	public static void main(String[] args) {

        SpringApplication.run(MsPedidosApplication.class, args);
	}

    /*@Autowired
    private Environment environment;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        //  URL "http://localhost:8081" desde el archivo de configuración
        return WebClient.builder().baseUrl(environment.getProperty("ms-productos.url"));
    }*/




}
