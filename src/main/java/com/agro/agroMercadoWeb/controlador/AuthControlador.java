package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.UsuarioDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    // Página de formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioDTO());
        model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
        return "registro";
    }

    @PostMapping("/crearUsuario")
    public String crearUsuario(@Valid @ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO,
                               BindingResult result,
                               Model model) {

        if (result.hasErrors()) {
            model.addAttribute("tiposUsuario", Usuario.TipoUsuario.values());
            return "registro";
        }

        // convertir DTO a entidad
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(usuarioDTO.getNombreCompleto());
        usuario.setCorreo(usuarioDTO.getCorreo());
        usuario.setContrasena(usuarioDTO.getContrasena());
        usuario.setTelefono(usuarioDTO.getTelefono());
        usuario.setDireccion(usuarioDTO.getDireccion());
        usuario.setTipoUsuario(Usuario.TipoUsuario.valueOf(usuarioDTO.getTipoUsuario().toUpperCase()));

        usuarioServicio.registrarUsuario(usuario);
        return "redirect:/login?registroExitoso";
    }
}
