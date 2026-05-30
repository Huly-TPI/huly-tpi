package com.huly.backend.infrastructure.security;

import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock private AppUserRepository appUserRepository;

    @InjectMocks private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithRoleAdmin() {
        AppUserEntity entity = AppUserEntity.builder()
                .id(1L).email("admin@huly.com").password("encoded").role(UserRole.ADMIN).build();
        when(appUserRepository.findByEmail("admin@huly.com")).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername("admin@huly.com");

        assertThat(result.getUsername()).isEqualTo("admin@huly.com");
        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithRoleUser() {
        AppUserEntity entity = AppUserEntity.builder()
                .id(2L).email("user@huly.com").password("encoded").role(UserRole.USER).build();
        when(appUserRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername("user@huly.com");

        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldDefaultToRoleUser_whenRoleIsNull() {
        AppUserEntity entity = AppUserEntity.builder()
                .id(3L).email("noRole@huly.com").password("encoded").role(null).build();
        when(appUserRepository.findByEmail("noRole@huly.com")).thenReturn(Optional.of(entity));

        UserDetails result = userDetailsService.loadUserByUsername("noRole@huly.com");

        assertThat(result.getAuthorities()).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        when(appUserRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing@huly.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@huly.com");
    }
}
