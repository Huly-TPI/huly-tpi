package com.huly.backend.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT que intercepta cada request para autenticar al usuario.
 * OncePerRequestFilter garantiza que se ejecuta exactamente una vez por request,
 * incluso en redirecciones internas de Spring.
 *
 * Flujo de autenticación:
 *  1. Extrae el token del header "Authorization: Bearer <token>"
 *  2. Si no hay header o el formato es incorrecto, deja pasar el request sin autenticar
 *  3. Valida la firma y expiración del JWT
 *  4. Carga el usuario desde BD y establece la autenticación en el SecurityContext
 *     (solo si no hay una autenticación previa para evitar trabajo redundante)
 *  5. Continúa la cadena de filtros
 *
 * Los endpoints públicos (auth/**, swagger/**) no necesitan pasar esta validación
 * porque SecurityConfig los permite antes de llegar a este filtro.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Sin header o formato incorrecto → request sin autenticar, Spring Security decidirá si es permitido
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrae el token quitando el prefijo "Bearer " (7 caracteres)
        String token = authHeader.substring(7);

        // Si el token está expirado o la firma es inválida, continúa sin autenticar
        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmail(token);

        // Solo autentica si hay email y no hay una autenticación previa en el contexto
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Crea el objeto de autenticación con los roles del usuario (credentials=null porque ya validamos el JWT)
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // Registra la autenticación en el contexto de seguridad del hilo actual
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
