package com.agro.agroMercadoWeb.servicio;

import com.agro.agroMercadoWeb.modelo.Usuario;

public interface UsuarioServicio {
    Usuario registrarUsuario(Usuario usuario);
    Usuario buscarPorCorreo(String correo);
}
