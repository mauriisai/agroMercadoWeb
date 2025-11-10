package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.EntregaDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/operador")
@RequiredArgsConstructor
public class EntregaControlador {

    private final UsuarioServicio usuarioServicio;
    private final ReservaServicio reservaServicio;

    @GetMapping("/entrega")
    public String mostrarFormularioEntrega(Model model, Authentication authentication) {
        try {
            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());

            if (reservas.isEmpty()) {
                return "redirect:/comprador/carrito";
            }

            BigDecimal totalGeneral = reservas.stream()
                    .map(ReservaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Si el usuario tiene dirección, usarla como valor por defecto
            String direccionDefault = comprador.getDireccion() != null ? comprador.getDireccion() : "";

            EntregaDTO entregaDTO = new EntregaDTO();
            entregaDTO.setModalidadEntrega("DOMICILIO");
            entregaDTO.setDireccionEntrega(direccionDefault);
            entregaDTO.setFechaEntrega(LocalDateTime.now().plusDays(1));

            model.addAttribute("entregaDTO", entregaDTO);
            model.addAttribute("reservas", reservas);
            model.addAttribute("totalGeneral", totalGeneral);

        } catch (Exception e) {
            model.addAttribute("error", "Error cargando formulario de entrega");
            return "redirect:/comprador/carrito";
        }

        return "operador/entrega";
    }

    @PostMapping("/procesar-entrega")
    public String procesarEntrega(@ModelAttribute EntregaDTO entregaDTO,
                                  Authentication authentication,
                                  HttpSession session) {
        try {
            System.out.println("entrega control");
            session.setAttribute("entregaData", entregaDTO);

            // Redirigir al pago
            return "redirect:/reserva/form-procesarPago";

        } catch (Exception e) {
            return "redirect:/operador/entrega?error=true";
        }
    }
}
