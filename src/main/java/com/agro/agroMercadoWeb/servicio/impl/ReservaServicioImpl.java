package com.agro.agroMercadoWeb.servicio.impl;

import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Producto;
import com.agro.agroMercadoWeb.modelo.Reserva;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.repositorio.ProductoRepositorio;
import com.agro.agroMercadoWeb.repositorio.ReservaRepositorio;
import com.agro.agroMercadoWeb.repositorio.UsuarioRepositorio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservaServicioImpl implements ReservaServicio {

    private final ReservaRepositorio reservaRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioRepositorio usuarioRepositorio;

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

    @Override
    public void agregarAlCarrito(Long compradorId, ReservaDTO dto) {
        Producto producto = productoRepositorio.findById(dto.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Usuario comprador = usuarioRepositorio.findById(compradorId)
                .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));

        BigDecimal total = dto.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.getCantidad()));

        Reserva reserva = Reserva.builder()
                .comprador(comprador)
                .producto(producto)
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .total(total)
                .estado(Reserva.EstadoReserva.valueOf("PENDIENTE"))
                .build();

        reservaRepositorio.save(reserva);
    }

    @Override
    public List<ReservaDTO> listarPendientesPorComprador(Long compradorId) {
        return reservaRepositorio.findByComprador_IdAndEstado(compradorId, Reserva.EstadoReserva.PENDIENTE)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    private ReservaDTO toDTO(Reserva reserva) {
        ReservaDTO dto = new ReservaDTO();
        dto.setId(reserva.getId());
        dto.setProductoId(reserva.getProducto().getId());
        dto.setProductoNombre(reserva.getProducto().getNombre());
        dto.setPrecioUnitario(reserva.getPrecioUnitario());
        dto.setCantidad(reserva.getCantidad());
        dto.setTotal(reserva.getTotal());
        dto.setEstado(String.valueOf(reserva.getEstado()));
        dto.setFechaReserva(reserva.getFechaReserva());

        // Datos del comprador
        if (reserva.getComprador() != null) {
            dto.setCompradorId(reserva.getComprador().getId());
            dto.setCompradorCorreo(reserva.getComprador().getCorreo());
        }
        return dto;
    }

    private Reserva toEntity(ReservaDTO dto) {
        Reserva reserva = new Reserva();

        reserva.setId(dto.getId());
        reserva.setCantidad(dto.getCantidad());
        reserva.setPrecioUnitario(dto.getPrecioUnitario());
        reserva.setTotal(dto.getTotal());
        reserva.setEstado(Reserva.EstadoReserva.valueOf(dto.getEstado()));
        reserva.setFechaReserva(dto.getFechaReserva());

        if (dto.getProductoId() != null) {
            Producto producto = productoRepositorio.findById(dto.getProductoId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
            reserva.setProducto(producto);
        }

        if (dto.getCompradorId() != null) {
            Usuario comprador = usuarioRepositorio.findById(dto.getCompradorId())
                    .orElseThrow(() -> new RuntimeException("Comprador no encontrado"));
            reserva.setComprador(comprador);
        }

        return reserva;
    }

    @Override
    public void cancelarReserva(Long id) {
        Reserva reserva = reservaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
        reservaRepositorio.save(reserva);
    }

    @Override
    public void confirmarReservasPorComprador(Long compradorId) {
        List<Reserva> pendientes = reservaRepositorio.findByComprador_IdAndEstado(
                compradorId, Reserva.EstadoReserva.PENDIENTE);

        if (pendientes.isEmpty()) {
            System.out.println("Reservas pagadas para confirmar.");
            return;
        }

        pendientes.forEach(reserva -> reserva.setEstado(Reserva.EstadoReserva.CONFIRMADA));
        reservaRepositorio.saveAll(pendientes);

        System.out.println(pendientes.size() + " reservas confirmadas para el comprador ID " + compradorId);
    }
}
