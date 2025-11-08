package com.agro.agroMercadoWeb.servicio;

import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;

public interface ReservaServicio {
    void crearReserva(ReservaDTO reservaDTO, Usuario comprador, boolean compraInmediata);
}
