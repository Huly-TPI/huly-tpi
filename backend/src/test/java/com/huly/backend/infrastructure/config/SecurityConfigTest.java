package com.huly.backend.infrastructure.config;

import com.huly.backend.infrastructure.security.JwtAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    // El HttpSecurity se mockea con RETURNS_SELF para que la cadena fluida
    // (csrf().sessionManagement()...addFilterBefore()) devuelva siempre el mismo mock,
    // y build() se stubea explicitamente para devolver la cadena final.
    @Mock(answer = Answers.RETURNS_SELF)
    private HttpSecurity httpSecurity;

    @Mock
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private SecurityFilterChain builtChain;

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig(jwtAuthFilter);
    }

    @Test
    @DisplayName("Devuelve la cadena construida por http.build() y registra el filtro JWT")
    void filterChainShouldReturnBuiltChainAndRegisterJwtFilter() throws Exception {
        // --- arrange ---
        givenHttpBuildsChain();

        // --- act ---
        SecurityFilterChain result = buildFilterChain();

        // --- assert ---
        thenReturnsBuiltChain(result);
        thenJwtFilterIsAddedBeforeUsernamePasswordFilter();
    }

    // Los lambdas internos authenticationEntryPoint (respuesta 401) y accessDeniedHandler
    // (respuesta 403) NO se ejecutan al construir la cadena: solo se registran como callbacks.
    // Su cuerpo (escritura de la respuesta JSON) es alcanzable unicamente ante peticiones web
    // reales no autenticadas / sin permisos, por lo que requiere un test de integracion web
    // (MockMvc / @SpringBootTest) y no se cubre en este test unitario.

    // --- arrange ---

    private void givenHttpBuildsChain() {
        when(httpSecurity.build()).thenReturn(builtChain);
    }

    // --- act ---

    private SecurityFilterChain buildFilterChain() throws Exception {
        return config.filterChain(httpSecurity);
    }

    // --- assert ---

    private void thenReturnsBuiltChain(SecurityFilterChain result) {
        assertThat(result).isSameAs(builtChain);
    }

    private void thenJwtFilterIsAddedBeforeUsernamePasswordFilter() {
        verify(httpSecurity).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
