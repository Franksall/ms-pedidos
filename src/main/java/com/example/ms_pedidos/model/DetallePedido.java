package com.example.ms_pedidos.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("detalle_pedidos") // Anotación R2DBC
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //  guía
    private Long id;

    @Column("pedido_id") // Indicamos R2DBC como se llama la columna de la llave foranea
    private Long pedidoId; // Reemplazamos el @ManyToOne

    private Long productoId;
    private Integer cantidad;
    private Double precioUnitario;
}