package com.huly.backend.infrastructure.config;

import com.huly.backend.infrastructure.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración principal de Spring Security.
 * Define qué endpoints son públicos, cuáles requieren autenticación,
 * y cómo responder ante errores de seguridad.
 *
 * Características principales:
 *  - STATELESS: sin sesiones HTTP (JWT es el único mecanismo de autenticación)
 *  - CSRF deshabilitado: correcto para APIs REST stateless que usan JWT (no cookies de sesión)
 *  - Autenticación por JWT via JwtAuthFilter (se agrega antes del filtro estándar de Spring)
 *  - @EnableMethodSecurity: permite usar @PreAuthorize en métodos de controladores
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // CSRF no aplica en APIs REST stateless con JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Sin HttpSession
                )
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS: los browsers envían OPTIONS antes de cada request cross-origin
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",        // Documentación de API
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/api/auth/**",          // Login, registro, refresh, logout
                                "/api/user-goals/**",    // NOTA: debería requerir auth (ver análisis de malas prácticas)
                                "/api/leads",            // Registro de interesados (público)
                                "/api/breathing/techniques" // Técnicas de respiración (contenido público)
                        ).permitAll()
                        .anyRequest().authenticated() // Todos los demás endpoints requieren JWT válido
                )
                // Respuestas JSON para errores de seguridad (en lugar de redirecciones HTML de Spring)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}"
                            );
                        })
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied\"}"
                            );
                        })
                )
                // Registra el filtro JWT antes del filtro de autenticación por usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
