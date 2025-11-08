package com.agro.agroMercadoWeb.modelo;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false, length = 100)
    private String nombreCompleto;

    @Column(nullable = false, unique = true, length = 100)
    private String correo;

    @Column(nullable = false, length = 200)
    private String contrasena;

    @Column(length = 20)
    private String telefono;

    @Column(length = 255)
    private String direccion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false, length = 20)
    private TipoUsuario tipoUsuario;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    public enum TipoUsuario {
        PRODUCTOR,
        COMPRADOR,
        ADMINISTRADOR,
        OPERADOR
    }
}
