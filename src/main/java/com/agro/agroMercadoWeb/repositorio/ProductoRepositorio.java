package com.agro.agroMercadoWeb.repositorio;

import com.agro.agroMercadoWeb.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    List<Producto> findByProductorId(Long productorId);
    List<Producto> findByActivoTrue();
}
