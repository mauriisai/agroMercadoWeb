package com.agro.agroMercadoWeb.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String metodo;  // "efectivo" o "tarjeta"
    private String titular;
    private String numTarjeta;
    private String expira;
    private String cvv;
    private BigDecimal monto;
    private String comprobanteId;
    private LocalDateTime fechaPago;

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL)
    private List<Reserva> reservas;  // un pago puede confirmar varias reservas
}

