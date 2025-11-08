package com.agro.agroMercadoWeb.servicio;

import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;

import java.util.List;

public interface ReservaServicio {
    void crearReserva(ReservaDTO reservaDTO, Usuario comprador, boolean compraInmediata);
    void agregarAlCarrito(Long compradorId, ReservaDTO reservaDTO);
    List<ReservaDTO> listarPendientesPorComprador(Long compradorId);
    void confirmarCompra(Long id);
    void cancelarReserva(Long id);
}
