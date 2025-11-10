package com.agro.agroMercadoWeb.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EntregaDTO {
    private String modalidadEntrega; // "DOMICILIO" o "RETIRO_EN_SITIO"
    private String direccionEntrega;
    private LocalDateTime fechaEntrega;
}
