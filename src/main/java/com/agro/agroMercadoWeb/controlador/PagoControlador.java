package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.PagoDTO;
import com.agro.agroMercadoWeb.dto.ReservaDTO;
import com.agro.agroMercadoWeb.modelo.Pago;
import com.agro.agroMercadoWeb.modelo.Reserva;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.PagoServicio;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.ReservaServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
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
    public String procesarPago(@ModelAttribute PagoDTO pagoDTO, Authentication authentication) {
        try {
            Usuario comprador = usuarioServicio.buscarPorCorreo(authentication.getName());
            List<ReservaDTO> reservas = reservaServicio.listarPendientesPorComprador(comprador.getId());

            // Armar el cuerpo para la API externa (usamos el total sumado)
                BigDecimal totalPagar = reservas.stream()
                    .map(r -> r.getTotal())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> pagoRequest = new HashMap<>();
            pagoRequest.put("tarjeta", pagoDTO.getNumTarjeta());
            pagoRequest.put("cvv", pagoDTO.getCvv());
            pagoRequest.put("fecha_expira", pagoDTO.getExpira());
            pagoRequest.put("titular", pagoDTO.getTitular());
            pagoRequest.put("comprador", comprador.getId());
            pagoRequest.put("total", totalPagar);
            pagoRequest.put("metodo", pagoDTO.getMetodo());

            // Enviar POST a la API externa
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

            // Leer el comprobante
            JsonNode node = mapper.readTree(response.getBody());
            String comprobanteId = node.get("id").asText();
            String fechaComprobante = node.get("createdAt").asText();

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

            // Confirmar reservas (pasamos por el servicio)
            reservaServicio.confirmarReservasPorComprador(comprador.getId());

            System.out.println("Pago registrado y " + reservas.size() + " reservas confirmadas.");
            System.out.println("Comprobante ID: " + comprobanteId);

        } catch (Exception e) {
            System.out.println("Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
        }

        return "comprador/carrito";
    }
}
