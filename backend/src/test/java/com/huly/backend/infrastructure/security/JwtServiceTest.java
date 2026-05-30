package com.huly.backend.infrastructure.security;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs256-algorithm-ok";
    private static final long ACCESS_MS = 3600000L;
    private static final long REFRESH_MS = 604800000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", ACCESS_MS);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpirationMs", REFRESH_MS);
        ReflectionTestUtils.setField(jwtService, "cookieSecure", false);
    }

    @Test
    void generateAccessToken_shouldBeValidAndContainEmailAndUserId() {
        String token = jwtService.generateAccessToken(1L, "user@test.com", UserRole.USER, UserStatus.ACTIVE);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void generateRefreshToken_shouldBeValidAndContainEmailAndUserId() {
        String token = jwtService.generateRefreshToken(1L, "user@test.com");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(1L);
    }

    @Test
    void isTokenValid_shouldReturnTrue_forFreshToken() {
        String token = jwtService.generateAccessToken(1L, "user@test.com", UserRole.ADMIN, UserStatus.ACTIVE);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forMalformedToken() {
        assertThat(jwtService.isTokenValid("not.a.valid.token")).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpirationMs", -1000L);
        String token = jwtService.generateAccessToken(1L, "user@test.com", UserRole.USER, UserStatus.ACTIVE);

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void getRefreshTokenMaxAgeSecs_shouldConvertMillisToSeconds() {
        assertThat(jwtService.getRefreshTokenMaxAgeSecs()).isEqualTo(REFRESH_MS / 1000);
    }

    @Test
    void isCookieSecure_shouldReturnConfiguredValue() {
        assertThat(jwtService.isCookieSecure()).isFalse();

        ReflectionTestUtils.setField(jwtService, "cookieSecure", true);
        assertThat(jwtService.isCookieSecure()).isTrue();
    }
}
