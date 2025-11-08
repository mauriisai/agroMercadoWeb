package com.agro.agroMercadoWeb.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "productos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal precioDetalle;

    @Column
    private BigDecimal precioMayoreo;

    @Column(nullable = false)
    private Integer cantidadDisponible;

    // Relación con la categoría
    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private CategoriaProducto categoria;

    // Relación con el productor (usuario)
    @ManyToOne(optional = false)
    @JoinColumn(name = "productor_id", nullable = false)
    private Usuario productor;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();
}
