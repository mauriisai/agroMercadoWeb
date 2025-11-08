package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.dto.UsuarioDTO;
import com.agro.agroMercadoWeb.modelo.Producto;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comprador")
public class CompradorControlador {

    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;
    private final ReservaServicio reservaServicio;

    @GetMapping("/index")
    public String mostrarDashboardComprador(Model model, Authentication authentication) {
        List<ProductoDTO> productos = productoServicio.listarActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("nombreUsuario", authentication.getName());

        return "comprador/index";
    }

    @GetMapping("/reservar/{productoId}")
    public String mostrarFormularioReserva(@PathVariable Long productoId, Model model) {
        ProductoDTO producto = productoServicio.buscarPorId(productoId);
        ReservaDTO reserva = new ReservaDTO();
        reserva.setProductoId(producto.getId());
        reserva.setProductoNombre(producto.getNombre());
        reserva.setPrecioUnitario(producto.getPrecioDetalle());
        model.addAttribute("reserva", reserva);
        return "comprador/form_reserva";
    }

    @PostMapping("/reservar")
    public String procesarReserva(@ModelAttribute("reserva") ReservaDTO reservaDTO,
                                  Authentication authentication) {
        Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
        reservaServicio.crearReserva(reservaDTO, comprador, false); // false = solo reserva
        return "redirect:/comprador/index?reservaExitosa";
    }

    @GetMapping("/comprar/{productoId}")
    public String mostrarFormularioCompra(@PathVariable Long productoId, Model model) {
        ProductoDTO producto = productoServicio.buscarPorId(productoId);
        ReservaDTO reserva = new ReservaDTO();
        reserva.setProductoId(producto.getId());
        reserva.setProductoNombre(producto.getNombre());
        reserva.setPrecioUnitario(producto.getPrecioDetalle());
        model.addAttribute("reserva", reserva);
        return "comprador/form_compra";
    }

    @PostMapping("/comprar")
    public String procesarCompra(@ModelAttribute("reserva") ReservaDTO reservaDTO,
                                 Authentication authentication) {
        Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
        reservaServicio.crearReserva(reservaDTO, comprador, true); // true = compra confirmada
        return "redirect:/comprador/index?compraExitosa";
    }
}
