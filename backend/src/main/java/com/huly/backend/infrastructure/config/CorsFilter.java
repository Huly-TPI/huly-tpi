package com.huly.backend.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component("customCorsFilter")
@Slf4j
public class CorsFilter extends OncePerRequestFilter {

    private final String frontendUrl;

    public CorsFilter(@Value("${frontend.url}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
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

        if (origin == null || !origin.equals(frontendUrl)) {
            log.warn("CORS bloqueado para origin: {}", origin);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String requestHeader = httpRequest.getHeader("Access-Control-Request-Headers");

        httpResponse.setHeader("Access-Control-Allow-Origin", frontendUrl);
        httpResponse.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
        httpResponse.addHeader("Access-Control-Allow-Headers", requestHeader);
        httpResponse.addHeader("Access-Control-Max-Age", "86400");

        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(httpRequest, httpResponse);
    }
}