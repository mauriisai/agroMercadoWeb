package com.agro.agroMercadoWeb.repositorio;

import com.agro.agroMercadoWeb.modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {
    List<Reserva> findByCompradorId(Long compradorId);

    // Devuelve las reservas según comprador y estado (ejemplo: PENDIENTE)
    List<Reserva> findByComprador_IdAndEstado(Long compradorId, Reserva.EstadoReserva estado);
}
