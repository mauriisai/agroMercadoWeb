package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/comprador")
public class CompradorControlador {

    private final ProductoServicio productoServicio;
    private final UsuarioServicio usuarioServicio;

    @GetMapping("/index")
    public String mostrarDashboardComprador(Model model, Authentication authentication) {
        List<ProductoDTO> productos = productoServicio.listarActivos();
        model.addAttribute("productos", productos);
        model.addAttribute("nombreUsuario", authentication.getName());
        return "comprador/index";
    }
}
