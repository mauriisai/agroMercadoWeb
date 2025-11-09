package com.agro.agroMercadoWeb.servicio.impl;

import com.agro.agroMercadoWeb.modelo.Pago;
import com.agro.agroMercadoWeb.repositorio.PagoRepositorio;
import com.agro.agroMercadoWeb.servicio.PagoServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PagoServicioImpl implements PagoServicio {

    private final PagoRepositorio pagoRepositorio;

    @Override
    public void guardar(Pago pago) {
        pagoRepositorio.save(pago);
    }
}
