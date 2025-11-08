package com.agro.agroMercadoWeb.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain seguridad(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registro", "/crearUsuario", "/css/**", "/images/**").permitAll()
                        .requestMatchers("/productor/**").hasRole("PRODUCTOR")
                        .requestMatchers("/comprador/**").hasRole("COMPRADOR")
                        .requestMatchers("/operador/**").hasRole("OPERADOR")
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .failureUrl("/login?error")      // <- asegura redirección en caso de error
                        .successHandler(customAuthenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    // Bean que define el comportamiento post-login
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var roles = authentication.getAuthorities();
            String redirectUrl = "/";

            if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMINISTRADOR"))) {
                redirectUrl = "/admin/index";
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_PRODUCTOR"))) {
                redirectUrl = "/productor/index";
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_COMPRADOR"))) {
                redirectUrl = "/comprador/index";
            } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_OPERADOR"))) {
                redirectUrl = "/operador/index";
            }

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
