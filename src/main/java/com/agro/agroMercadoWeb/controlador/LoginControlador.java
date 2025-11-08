package com.agro.agroMercadoWeb.controlador;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginControlador {
    @GetMapping("/")
    public String redirigirPorRol(Authentication auth) {
        if (auth == null) return "redirect:/login";

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")))
            return "redirect:/admin/index";

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PRODUCTOR")))
            return "redirect:/productor/index";

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COMPRADOR")))
            return "redirect:/comprador/index";

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_OPERADOR")))
            return "redirect:/operador/index";

        return "redirect:/login";
    }
}
