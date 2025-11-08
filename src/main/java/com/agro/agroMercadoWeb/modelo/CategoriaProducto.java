package com.agro.agroMercadoWeb.modelo;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "categorias_producto")
@Data
public class CategoriaProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    private String descripcion;
}

