package com.agro.agroMercadoWeb.repositorio;

import com.agro.agroMercadoWeb.modelo.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservaRepositorio extends JpaRepository<Reserva, Long> {
    List<Reserva> findByCompradorId(Long compradorId);
}
