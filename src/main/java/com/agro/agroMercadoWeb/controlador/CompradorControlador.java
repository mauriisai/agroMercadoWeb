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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
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

    @GetMapping("/carrito")
    public String verCarrito(Model model, Authentication authentication) {
        try {
            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());

            BigDecimal totalGeneral = reservas.stream()
                    .map(ReservaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("reservas", reservas);
            model.addAttribute("totalGeneral", totalGeneral);

        } catch (Exception e) {
            model.addAttribute("error", "Error cargando carrito: " + e.getMessage());
        }

        return "comprador/carrito";
    }
}
