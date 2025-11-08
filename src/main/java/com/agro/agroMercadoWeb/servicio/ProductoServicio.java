package com.agro.agroMercadoWeb.servicio;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import java.util.List;

public interface ProductoServicio {
    List<ProductoDTO> listarActivos();
    List<ProductoDTO> listarPorProductor(Long productorId);
    void guardar(ProductoDTO productoDTO);

    ProductoDTO buscarPorId(Long id);
    void actualizar(ProductoDTO productoDTO);
    boolean cambiarEstado(Long id);
}

