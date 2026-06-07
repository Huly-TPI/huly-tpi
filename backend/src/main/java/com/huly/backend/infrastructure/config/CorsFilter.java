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

@Component("customCorsFilter")
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    private final String frontendUrl;
    private final String landingUrl;

    public CorsFilter(
            @Value("${frontend.url}") String frontendUrl,
            @Value("${landing.url}") String landingUrl) {
        this.frontendUrl = frontendUrl;
        this.landingUrl = landingUrl;
    }

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

        // Sin Origin → no es un request de browser, dejar pasar
        if (origin == null) {
            chain.doFilter(httpRequest, httpResponse);
            return;
        }
        // Con Origin pero no coincide → bloquear
        if (!origin.equals(frontendUrl) && !origin.equals(landingUrl)) {
            log.warn("CORS bloqueado para origin: {}", origin);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestHeader = httpRequest.getHeader("Access-Control-Request-Headers");

        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.addHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, PATCH, OPTIONS, DELETE");
        httpResponse.addHeader("Access-Control-Allow-Headers", requestHeader != null ? requestHeader : "Authorization, Content-Type");
        httpResponse.addHeader("Access-Control-Max-Age", "86400");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }
}