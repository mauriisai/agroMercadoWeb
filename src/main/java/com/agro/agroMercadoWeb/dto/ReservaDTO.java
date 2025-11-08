package com.agro.agroMercadoWeb.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservaDTO {

    private Long id;

    private Long compradorId;
    private String compradorCorreo;

    private Long productoId;
    private String productoNombre;

    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal total;

    private String estado;
    private LocalDateTime fechaReserva;
}

