package com.huly.backend.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class CorsFilterTest {

    private static final String FRONTEND_URL = "http://localhost:5173";

    private CorsFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new CorsFilter(FRONTEND_URL);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    void shouldNotFilter_shouldSkipSwaggerUiPath() throws ServletException, IOException {
        request.setRequestURI("/swagger-ui/index.html");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldNotFilter_shouldSkipApiDocsPath() throws ServletException, IOException {
        request.setRequestURI("/v3/api-docs/swagger-config");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_shouldPassThroughWithoutCorsHeaders_whenNoOriginHeader() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Access-Control-Allow-Origin")).isNull();
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void doFilter_shouldReturn403_whenOriginDoesNotMatchFrontendUrl() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.addHeader("Origin", "http://malicious.com");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(filterChain.getRequest()).isNull();
    }

    @Test
    void doFilter_shouldReturn200WithCorsHeaders_whenOptionsRequestFromValidOrigin() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.setMethod("OPTIONS");
        request.addHeader("Origin", FRONTEND_URL);
        request.addHeader("Access-Control-Request-Headers", "Authorization");

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(FRONTEND_URL);
        assertThat(response.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(response.getHeader("Access-Control-Allow-Headers")).isEqualTo("Authorization");
        assertThat(filterChain.getRequest()).isNull();
    }

    @Test
    void doFilter_shouldUseDefaultAllowHeaders_whenRequestHeaderIsAbsent() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.setMethod("OPTIONS");
        request.addHeader("Origin", FRONTEND_URL);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Access-Control-Allow-Headers"))
                .isEqualTo("Authorization, Content-Type");
    }

    @Test
    void doFilter_shouldPassThroughWithCorsHeaders_whenRegularRequestFromValidOrigin() throws ServletException, IOException {
        request.setRequestURI("/api/auth/login");
        request.setMethod("POST");
        request.addHeader("Origin", FRONTEND_URL);

        filter.doFilter(request, response, filterChain);

        assertThat(response.getHeader("Access-Control-Allow-Origin")).isEqualTo(FRONTEND_URL);
        assertThat(response.getHeader("Access-Control-Allow-Credentials")).isEqualTo("true");
        assertThat(filterChain.getRequest()).isNotNull();
    }
}
