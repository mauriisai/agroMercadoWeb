package com.agro.agroMercadoWeb.servicio.impl;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.modelo.CategoriaProducto;
import com.agro.agroMercadoWeb.modelo.Producto;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.repositorio.CategoriaProductoRepositorio;
import com.agro.agroMercadoWeb.repositorio.ProductoRepositorio;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServicioImpl implements ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final CategoriaProductoRepositorio categoriaProductoRepositorio;

    @Override
    public List<ProductoDTO> listarActivos() {
        return productoRepositorio.findByActivoTrue()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductoDTO> listarPorProductor(Long productorId) {
        List<Producto> productos = productoRepositorio.findByProductorId(productorId);
        return productos.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    @Override
    public void guardar(ProductoDTO productoDTO) {
        Producto producto = new Producto();
        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecioDetalle(productoDTO.getPrecioDetalle());
        producto.setPrecioMayoreo(productoDTO.getPrecioMayoreo());
        producto.setCantidadDisponible(productoDTO.getCantidadDisponible());

        CategoriaProducto categoria = categoriaProductoRepositorio.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        Usuario productor = new Usuario();
        productor.setId(productoDTO.getProductorId());
        producto.setProductor(productor);

        productoRepositorio.save(producto);
    }

    @Override
    public ProductoDTO buscarPorId(Long id) {
        Producto producto = productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        return convertirADTO(producto);
    }

    @Override
    public void actualizar(ProductoDTO productoDTO) {
        Producto producto = productoRepositorio.findById(productoDTO.getId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        producto.setNombre(productoDTO.getNombre());
        producto.setDescripcion(productoDTO.getDescripcion());
        producto.setPrecioDetalle(productoDTO.getPrecioDetalle());
        producto.setPrecioMayoreo(productoDTO.getPrecioMayoreo());
        producto.setCantidadDisponible(productoDTO.getCantidadDisponible());

        CategoriaProducto categoria = categoriaProductoRepositorio.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        producto.setCategoria(categoria);

        productoRepositorio.save(producto);
    }

    @Override
    public boolean cambiarEstado(Long id) {
        Producto producto = productoRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Cambia el estado actual
        producto.setActivo(!producto.getActivo());
        productoRepositorio.save(producto);

        // Retorna el nuevo estado (true = activo, false = inactivo)
        return producto.getActivo();
    }

    private ProductoDTO convertirADTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecioDetalle(producto.getPrecioDetalle());
        dto.setPrecioMayoreo(producto.getPrecioMayoreo());
        dto.setCantidadDisponible(producto.getCantidadDisponible());
        dto.setActivo(producto.getActivo());
        if (producto.getCategoria() != null) {
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
            dto.setCategoriaId(producto.getCategoria().getId());
        }

        if (producto.getProductor() != null) {
            dto.setProductorId(producto.getProductor().getId());
            dto.setProductorCorreo(producto.getProductor().getCorreo());
        }
        return dto;
    }
}
