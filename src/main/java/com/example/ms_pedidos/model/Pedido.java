package com.example.ms_pedidos.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("pedidos") // Anotación R2DBC para la tabla
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //  guía
    private Long id;

    private String cliente;
    private LocalDateTime fecha;
    private Double total;
    private String estado; // PENDIENTE, PROCESADO, CANCELADO


    @Transient
    private List<DetallePedido> detalles;
}