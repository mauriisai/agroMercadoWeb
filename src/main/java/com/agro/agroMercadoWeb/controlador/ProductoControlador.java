package com.agro.agroMercadoWeb.controlador;

import com.agro.agroMercadoWeb.dto.ProductoDTO;
import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.repositorio.CategoriaProductoRepositorio;
import com.agro.agroMercadoWeb.servicio.ProductoServicio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/productor")
@RequiredArgsConstructor
public class ProductoControlador {

    private final ProductoServicio productoServicio;
    private final CategoriaProductoRepositorio categoriaProductoRepositorio;
    private final UsuarioServicio usuarioServicio;

    @GetMapping("/publicar")
    public String mostrarFormulario(Model model) {
        model.addAttribute("productoDTO", new ProductoDTO());
        model.addAttribute("categorias", categoriaProductoRepositorio.findAll());
        return "productor/publicar_producto";
    }

    @PostMapping("/publicar")
    public String guardarProducto(@Valid @ModelAttribute("productoDTO") ProductoDTO productoDTO,
                                  BindingResult result,
                                  Authentication authentication,
                                  Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaProductoRepositorio.findAll());
            return "productor/publicar_producto";
        }

        String correo = authentication.getName();
        Usuario productor = usuarioServicio.buscarPorCorreo(correo);

        productoDTO.setProductorId(productor.getId());
        productoServicio.guardar(productoDTO);

        return "redirect:/productor/index";
    }

    @GetMapping("/index")
    public String mostrarDashboard(Model model, Authentication authentication) {
        String correo = authentication.getName();
        Usuario productor = usuarioServicio.buscarPorCorreo(correo);

        List<ProductoDTO> productos = productoServicio.listarPorProductor(productor.getId());
        model.addAttribute("productos", productos);
        model.addAttribute("nombreUsuario", productor.getNombreCompleto());

        return "productor/index";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        ProductoDTO productoDTO = productoServicio.buscarPorId(id);
        model.addAttribute("productoDTO", productoDTO);
        model.addAttribute("categorias", categoriaProductoRepositorio.findAll());
        return "productor/editar_producto";
    }

    @PostMapping("/editar/{id}")
    public String actualizarProducto(@PathVariable Long id,
                                     @Valid @ModelAttribute("productoDTO") ProductoDTO productoDTO,
                                     BindingResult result,
                                     Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaProductoRepositorio.findAll());
            return "productor/editar_producto";
        }

        productoDTO.setId(id);
        productoServicio.actualizar(productoDTO);
        return "redirect:/productor/index";
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean activo = productoServicio.cambiarEstado(id);

        if (activo) {
            redirectAttributes.addFlashAttribute("mensaje", "Publicación habilitada correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Publicación inactivada correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "warning");
        }

        return "redirect:/productor/index";
    }
}
