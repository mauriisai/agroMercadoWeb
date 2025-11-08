package com.agro.agroMercadoWeb.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio al detalle es obligatorio")
    @DecimalMin(value = "0.1", message = "El precio debe ser mayor que 0")
    private BigDecimal precioDetalle;

    private BigDecimal precioMayoreo;

    @NotNull(message = "Debe especificar la cantidad disponible")
    @Min(value = 1, message = "Debe haber al menos una unidad disponible")
    private Integer cantidadDisponible;

    @NotNull(message = "Debe seleccionar una categoría")
    private Long categoriaId;  // sólo guardamos el id, no el objeto

    private String categoriaNombre;  // sólo guardamos el id, no el objeto

    private Boolean activo;

    private Long productorId;  // se asignará automáticamente según el login

    private String productorCorreo;  // se asignará automáticamente según el login
}

