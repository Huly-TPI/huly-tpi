package com.huly.backend.infrastructure.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtro CORS personalizado que reemplaza la configuración estándar de Spring.
 * Se registra con la máxima prioridad (@Order(HIGHEST_PRECEDENCE)) para actuar
 * antes que cualquier otro filtro, incluido el de seguridad de Spring.
 *
 * Política de CORS:
 *  - Solo permite requests del frontend configurado (frontend.url en application.properties)
 *  - Sin header Origin → request no-browser (Postman, cron, etc.) → se permite sin verificación CORS
 *  - Origin diferente al frontend → 403 Forbidden (bloqueo explícito)
 *  - Origin correcto → se agregan los headers CORS necesarios
 *
 * Access-Control-Allow-Credentials=true: necesario para que el navegador envíe la
 * cookie del refreshToken en requests cross-origin.
 *
 * Se excluye Swagger (/swagger-ui, /v3/api-docs) para no interferir con las herramientas de dev.
 */
@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    private final String frontendUrl;

    public CorsFilter(@Value("${frontend.url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    /** Swagger usa sus propios headers CORS: excluimos esas rutas para no interferir. */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpRequest,
                                    HttpServletResponse httpResponse,
                                    FilterChain chain) throws ServletException, IOException {

        String origin = httpRequest.getHeader("Origin");

        // Sin Origin → no es un request de browser, no aplica CORS, se deja pasar
        if (origin == null) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }

        // Origin diferente al frontend configurado → bloquear con 403
        if (!origin.equals(frontendUrl)) {
            log.warn("CORS bloqueado para origin: {}", origin);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Refleja los headers que el navegador solicita (preflight) para mayor compatibilidad
        String requestHeader = httpRequest.getHeader("Access-Control-Request-Headers");

        httpResponse.setHeader("Access-Control-Allow-Origin", frontendUrl);
        httpResponse.addHeader("Access-Control-Allow-Credentials", "true"); // Necesario para enviar cookies cross-origin
        httpResponse.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, PATCH, OPTIONS, DELETE");
        httpResponse.addHeader("Access-Control-Allow-Headers", requestHeader != null ? requestHeader : "Authorization, Content-Type");
        httpResponse.addHeader("Access-Control-Max-Age", "86400"); // Cache de preflight por 24 horas

        // Requests OPTIONS son preflight del browser: se responde OK sin pasar al controlador
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }
}