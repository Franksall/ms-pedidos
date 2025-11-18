package com.example.ms_pedidos.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

//  DTO representa la respuesta que esperamos de ms-productos
@Data
@NoArgsConstructor
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer stock;
    private Boolean activo;
}