package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.EntregaDTO;
import com.agro.agroMercadoWeb.dto.PagoDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Pago;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.PagoServicio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/pagar")
@RequiredArgsConstructor
public class PagoControlador {

    private final UsuarioServicio usuarioServicio;
    private final ReservaServicio reservaServicio;
    private final PagoServicio pagoServicio;

    @PostMapping
    @Transactional
    public String procesarPago(@ModelAttribute("pago") PagoDTO pagoDTO,
                               Authentication authentication, HttpSession session,
                               Model model) {
        try {
            System.out.println("INICIANDO PROCESO DE PAGO - Método: " + pagoDTO.getMetodo());

            // Recuperar datos de entrega
            EntregaDTO entregaDTO = (EntregaDTO) session.getAttribute("entregaData");

            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());

            if (reservas.isEmpty()) {
                model.addAttribute("error", "No hay reservas pendientes para pagar");
                return cargarFormularioPago(model, authentication);
            }

            BigDecimal totalPagar = reservas.stream()
                    .map(ReservaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String comprobanteId;
            String fechaComprobante;

            if ("tarjeta".equals(pagoDTO.getMetodo())) {
                System.out.println("PROCESANDO PAGO CON TARJETA");

                // Validar que los datos de tarjeta no sean nulos
                if (pagoDTO.getNumTarjeta() == null || pagoDTO.getCvv() == null ||
                        pagoDTO.getExpira() == null || pagoDTO.getTitular() == null) {
                    throw new IllegalArgumentException("Datos de tarjeta incompletos");
                }

                // Validaciones específicas de formato
                if (!pagoDTO.getNumTarjeta().matches("\\d{16}")) {
                    throw new IllegalArgumentException("El número de tarjeta debe tener 16 dígitos");
                }
                if (!pagoDTO.getExpira().matches("(0[1-9]|1[0-2])/?([0-9]{2})")) {
                    throw new IllegalArgumentException("La fecha de expiración debe tener formato MM/YY");
                }
                if (!pagoDTO.getCvv().matches("\\d{3}")) {
                    throw new IllegalArgumentException("El CVV debe tener 3 dígitos");
                }

                Map<String, Object> pagoRequest = new HashMap<>();
                pagoRequest.put("tarjeta", pagoDTO.getNumTarjeta());
                pagoRequest.put("cvv", pagoDTO.getCvv());
                pagoRequest.put("fecha_expira", pagoDTO.getExpira());
                pagoRequest.put("titular", pagoDTO.getTitular());
                pagoRequest.put("comprador", comprador.getId());
                pagoRequest.put("total", totalPagar);
                pagoRequest.put("metodo", pagoDTO.getMetodo());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.add("x-api-key", "reqres-free-v1");

                ObjectMapper mapper = new ObjectMapper();
                String pagoJson = mapper.writeValueAsString(pagoRequest);

                HttpEntity<String> request = new HttpEntity<>(pagoJson, headers);
                RestTemplate restTemplate = new RestTemplate();

                ResponseEntity<String> response = restTemplate.postForEntity(
                        "https://reqres.in/api/payments",
                        request,
                        String.class
                );

                // Validar respuesta
                if (!response.getStatusCode().is2xxSuccessful()) {
                    throw new RuntimeException("Error en la pasarela de pago");
                }

                JsonNode node = mapper.readTree(response.getBody());
                comprobanteId = node.get("id").asText();
                fechaComprobante = node.get("createdAt").asText();

            } else {
                System.out.println("PROCESANDO PAGO EN EFECTIVO");
                // Proceso para EFECTIVO (generar datos locales)
                comprobanteId = String.format("%05d", new Random().nextInt(100000));
                fechaComprobante = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
            }

            // Crear el pago local
            Pago nuevoPago = Pago.builder()
                    .metodo(pagoDTO.getMetodo())
                    .titular(pagoDTO.getTitular())
                    .numTarjeta(pagoDTO.getNumTarjeta())
                    .expira(pagoDTO.getExpira())
                    .cvv(pagoDTO.getCvv())
                    .monto(totalPagar)
                    .comprobanteId(comprobanteId)
                    .fechaPago(LocalDateTime.parse(fechaComprobante, DateTimeFormatter.ISO_DATE_TIME))
                    .build();

            pagoServicio.guardar(nuevoPago);

            // Confirmar reservas
            reservaServicio.confirmarReservasPorComprador(comprador.getId(), nuevoPago);

            // Llamar Microservicio de Pedidos
            if (entregaDTO != null) {
                crearPedidosEnMicroservicio(reservas, comprador, entregaDTO, nuevoPago);
                session.removeAttribute("entregaData"); // Limpiar sesión
            }

            System.out.println("PAGO EXITOSO - " + reservas.size() + " reservas confirmadas. Comprobante: " + comprobanteId);

            return "redirect:/comprador/carrito?pagoExitoso=true";

        } catch (Exception e) {
            System.out.println("ERROR EN PAGO: " + e.getMessage());
            e.printStackTrace();

            // Agregar error al modelo y recargar el formulario
            model.addAttribute("error", e.getMessage());
            return cargarFormularioPago(model, authentication);
        }
    }

    private void crearPedidosEnMicroservicio(List<ReservaDTO> reservas, Usuario comprador,
                                             EntregaDTO entregaDTO, Pago pago) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            System.out.println("probando api");

            for (ReservaDTO reserva : reservas) {
                Map<String, Object> pedidoRequest = new HashMap<>();
                pedidoRequest.put("reservaId", reserva.getId());
                pedidoRequest.put("modalidadEntrega", entregaDTO.getModalidadEntrega());
                pedidoRequest.put("direccionEntrega", entregaDTO.getDireccionEntrega());
                pedidoRequest.put("fechaEntrega", entregaDTO.getFechaEntrega().toString());
                pedidoRequest.put("operadorId", null);
                pedidoRequest.put("pagoId", pago.getId());

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                ObjectMapper mapper = new ObjectMapper();
                String pedidoJson = mapper.writeValueAsString(pedidoRequest);

                HttpEntity<String> request = new HttpEntity<>(pedidoJson, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(
                        "http://localhost:8082/api/pedidos",
                        request,
                        String.class
                );

                System.out.println("Pedido creado para reserva: " + reserva.getId());
            }
        } catch (Exception e) {
            System.out.println("Error creando pedidos: " + e.getMessage());
        }
    }

    // MÉTODO AUXILIAR PARA CARGAR FORMULARIO
    private String cargarFormularioPago(Model model, Authentication authentication) {
        try {
            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());
            BigDecimal totalGeneral = reservas.stream()
                    .map(ReservaDTO::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            model.addAttribute("reservas", reservas);
            model.addAttribute("totalGeneral", totalGeneral);

            // Si no hay un objeto pago en el modelo, crear uno nuevo
            if (!model.containsAttribute("pago")) {
                model.addAttribute("pago", new PagoDTO());
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error cargando datos: " + e.getMessage());
        }

        return "form_procesarPago";
    }
}