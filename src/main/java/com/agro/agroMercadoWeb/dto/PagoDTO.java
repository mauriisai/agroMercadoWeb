package com.agro.agroMercadoWeb.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagoDTO {
    private String metodo;
    private String titular;
    private String numTarjeta;
    private String expira;
    private String cvv;
    private BigDecimal monto;
    private List<Long> reservasIds; // varias reservas asociadas
}

