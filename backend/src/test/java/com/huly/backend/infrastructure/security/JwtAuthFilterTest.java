package com.huly.backend.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsService userDetailsService;

    @InjectMocks private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldPassThrough_whenNoAuthorizationHeader() throws Exception {
        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldPassThrough_whenAuthHeaderIsNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        verifyNoInteractions(jwtService);
    }

    @Test
    void doFilter_shouldPassThrough_whenTokenIsInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer invalidToken");
        when(jwtService.isTokenValid("invalidToken")).thenReturn(false);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_shouldPassThrough_whenTokenIsNotAccessType() throws Exception {
        request.addHeader("Authorization", "Bearer refreshToken");
        when(jwtService.isTokenValid("refreshToken")).thenReturn(true);
        when(jwtService.isAccessToken("refreshToken")).thenReturn(false);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void doFilter_shouldSetAuthentication_whenTokenIsValidAccessToken() throws Exception {
        UserDetails userDetails = User.builder()
                .username("user@huly.com").password("pass").roles("USER").build();

        request.addHeader("Authorization", "Bearer validToken");
        when(jwtService.isTokenValid("validToken")).thenReturn(true);
        when(jwtService.isAccessToken("validToken")).thenReturn(true);
        when(jwtService.extractEmail("validToken")).thenReturn("user@huly.com");
        when(userDetailsService.loadUserByUsername("user@huly.com")).thenReturn(userDetails);

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertThat(filterChain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("user@huly.com");
    }

    @Test
    void doFilter_shouldNotOverrideExistingAuthentication_whenAlreadySet() throws Exception {
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken("existing@huly.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        request.addHeader("Authorization", "Bearer validToken");
        when(jwtService.isTokenValid("validToken")).thenReturn(true);
        when(jwtService.isAccessToken("validToken")).thenReturn(true);
        when(jwtService.extractEmail("validToken")).thenReturn("user@huly.com");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("existing@huly.com");
    }
}