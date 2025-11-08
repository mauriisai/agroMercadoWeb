package com.agro.agroMercadoWeb.servicio.impl;

import com.agro.agroMercadoWeb.modelo.Usuario;
import com.agro.agroMercadoWeb.repositorio.UsuarioRepositorio;
import com.agro.agroMercadoWeb.servicio.UsuarioServicio;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioServicioImpl implements UsuarioServicio, UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuario.getTipoUsuario() == Usuario.TipoUsuario.ADMINISTRADOR) {
            throw new IllegalArgumentException("No se permite crear usuarios Administradores desde el registro público.");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setActivo(true);
        return usuarioRepositorio.save(usuario);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepositorio.findByCorreo(correo).orElse(null);
    }

    // Este método es usado por Spring Security en el login
    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        try {
            System.out.println("Intentando autenticar usuario: " + correo);

            Usuario usuario = usuarioRepositorio.findByCorreo(correo)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

            System.out.println("Usuario encontrado: " + usuario.getCorreo() + " - Rol: " + usuario.getTipoUsuario());

            return User.builder()
                    .username(usuario.getCorreo())
                    .password(usuario.getContrasena())
                    .roles(usuario.getTipoUsuario().name())
                    .build();

        } catch (UsernameNotFoundException e) {
            System.err.println("Error: usuario no encontrado -> " + correo);
            throw e; // deja que Spring maneje el error normal
        } catch (Exception e) {
            System.err.println("Error inesperado en loadUserByUsername: " + e.getMessage());
            e.printStackTrace();
            throw new UsernameNotFoundException("Error interno al autenticar usuario");
        }
    }
}
