package com.agro.agroMercadoWeb.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UsuarioDTO {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombreCompleto;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,16}$",
            message = "Debe tener entre 8 y 16 caracteres, al menos una mayúscula, una minúscula y un número"
    )
    private String contrasena;

    @Pattern(regexp = "^[0-9]{8}$", message = "El teléfono debe tener exactamente 8 dígitos numéricos")
    private String telefono;

    private String direccion;

    private String tipoUsuario;
}

