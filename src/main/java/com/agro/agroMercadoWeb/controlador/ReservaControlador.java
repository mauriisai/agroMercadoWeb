package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reserva")
public class ReservaControlador {

    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;
    private final ReservaServicio reservaServicio;

    @GetMapping("/nueva/{productoId}")
    public String mostrarFormularioReserva(@PathVariable Long productoId, Model model) {
        ProductoDTO producto = productoServicio.buscarPorId(productoId);
        ReservaDTO reserva = new ReservaDTO();
        reserva.setProductoId(producto.getId());
        reserva.setProductoNombre(producto.getNombre());
        reserva.setPrecioUnitario(producto.getPrecioDetalle());
        reserva.setCantidad(1);
        model.addAttribute("reserva", reserva);
        return "comprador/form_reserva";
    }

    @PostMapping("/guardar")
    public String guardarReserva(@ModelAttribute("reserva") ReservaDTO reservaDTO,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {

        Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
        reservaServicio.agregarAlCarrito(comprador.getId(), reservaDTO);

        redirectAttributes.addFlashAttribute("mensaje", "Producto agregado al carrito correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");

        return "redirect:/reserva/carrito";
    }

    @GetMapping("/carrito")
    public String mostrarCarrito(Model model, Authentication authentication) {
        Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
        List<ReservaDTO> carrito = reservaServicio.listarPendientesPorComprador(comprador.getId());
        model.addAttribute("reservas", carrito);
        model.addAttribute("nombreUsuario", comprador.getNombreCompleto());
        return "comprador/carrito";
    }

    @PostMapping("/confirmar/{id}")
    public String confirmarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservaServicio.confirmarCompra(id);
        redirectAttributes.addFlashAttribute("mensaje", "Reserva confirmada exitosamente.");
        redirectAttributes.addFlashAttribute("tipo", "success");
        return "redirect:/reserva/carrito";
    }

    @PostMapping("/eliminar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservaServicio.cancelarReserva(id);
        redirectAttributes.addFlashAttribute("mensaje", "La reserva fue cancelada correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "warning");
        return "redirect:/reserva/carrito";
    }
}
