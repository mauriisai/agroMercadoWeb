package com.agro.agroMercadoWeb.servicio.impl;

import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Producto;
import com.agro.agroMercadoWeb.modelo.Reserva;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.repositorio.ProductoRepositorio;
import com.agro.agroMercadoWeb.repositorio.ReservaRepositorio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ReservaServicioImpl implements ReservaServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final ProductoRepositorio productoRepositorio;

    @Override
    public void crearReserva(ReservaDTO reservaDTO, Usuario comprador, boolean compraInmediata) {
        Producto producto = productoRepositorio.findById(reservaDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        BigDecimal total = reservaDTO.getPrecioUnitario()
                .multiply(BigDecimal.valueOf(reservaDTO.getCantidad()));

        Reserva reserva = Reserva.builder()
                .producto(producto)
                .comprador(comprador)
                .cantidad(reservaDTO.getCantidad())
                .precioUnitario(reservaDTO.getPrecioUnitario())
                .total(total)
                .estado(compraInmediata
                        ? Reserva.EstadoReserva.CONFIRMADA
                        : Reserva.EstadoReserva.PENDIENTE)
                .build();

        reservaRepositorio.save(reserva);
    }
}
