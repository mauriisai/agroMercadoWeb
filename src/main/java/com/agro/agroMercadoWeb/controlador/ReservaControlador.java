package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.EntregaDTO;
import com.agro.agroMercadoWeb.dto.PagoDTO;
import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Reserva;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/form-procesarPago")
    public String mostrarFormularioPago(Model model, Authentication authentication,
                                        HttpSession session) {
        try {
            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());

            System.out.println("entra luego de entrega");

            // Recuperar datos de entrega
            EntregaDTO entregaDTO = (EntregaDTO) session.getAttribute("entregaData");

            if (entregaDTO != null) {
                System.out.println("   - Dirección: " + entregaDTO.getDireccionEntrega());
                model.addAttribute("entregaData", entregaDTO);
            }

            // Calcular total general
            BigDecimal totalGeneral = reservas.stream()
                    .map(ReservaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("reservas", reservas);
            model.addAttribute("totalGeneral", totalGeneral);
            model.addAttribute("pago", new PagoDTO());

            return "reserva/form_procesarPago";

        } catch (Exception e) {
            return "redirect:/comprador/carrito";
        }
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

        // Calcular total general
        BigDecimal totalGeneral = carrito.stream()
                .map(ReservaDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("reservas", carrito);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("nombreUsuario", comprador.getNombreCompleto());

        return "comprador/carrito";
    }

    @PostMapping("/pagar")
    public String procesarPago(Authentication authentication) {
        try {
            // Datos simulados del pago (puedes calcular el total real si lo deseas)
            Map<String, Object> pago = new HashMap<>();
            pago.put("tarjeta", "432233221155");
            pago.put("comprador", 1); // o el ID real del comprador autenticado
            pago.put("total", 52.75);
            pago.put("metodo", "efectivo");

            // Configurar cabeceras HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-api-key", "reqres-free-v1"); // cabecera obligatoria

            // Combinar headers + body
            ObjectMapper mapper = new ObjectMapper();
            String pagoJson = mapper.writeValueAsString(pago);
            System.out.println("JSON que se enviará: " + pagoJson);
            HttpEntity<String> request = new HttpEntity<>(pagoJson, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://reqres.in/api/payments",
                    request,
                    String.class
            );

            // Mostrar respuesta en consola
            System.out.println("Pago procesado con éxito:");
            System.out.println(response.getBody());

        } catch (Exception e) {
            System.out.println("Error al comunicarse con la API-pagos: " + e.getMessage());
        }
        // Redirige de vuelta al carrito
        return "comprador/carrito";
    }

    @PostMapping("/eliminar/{id}")
    public String cancelarReserva(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        reservaServicio.cancelarReserva(id);
        redirectAttributes.addFlashAttribute("mensaje", "La reserva fue cancelada correctamente.");
        redirectAttributes.addFlashAttribute("tipo", "warning");
        return "redirect:/reserva/carrito";
    }

}
