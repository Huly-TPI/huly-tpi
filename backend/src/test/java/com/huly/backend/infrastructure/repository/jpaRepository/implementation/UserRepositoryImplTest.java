package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.domain.model.AppUser;
import com.huly.backend.domain.model.enums.UserRole;
import com.huly.backend.domain.model.enums.UserStatus;
import com.huly.backend.infrastructure.repository.entity.AppUserEntity;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.AppUserRepository;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.huly.backend.domain.model.enums.SourceAction;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock private AppUserRepository jpaRepository;
    @Mock private UserDetailRepository userDetailRepository;

    @InjectMocks private UserRepositoryImpl userRepository;

    @Test
    void findByEmail_shouldReturnMappedDomain_whenEntityExists() {
        AppUserEntity entity = AppUserEntity.builder()
                .id(1L).email("user@huly.com").password("encoded")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        when(jpaRepository.findByEmail("user@huly.com")).thenReturn(Optional.of(entity));

        Optional<AppUser> result = userRepository.findByEmail("user@huly.com");

        assertThat(result).isPresent();
        AppUser user = result.get();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("user@huly.com");
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getRole()).isEqualTo(UserRole.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEntityDoesNotExist() {
        when(jpaRepository.findByEmail("missing@huly.com")).thenReturn(Optional.empty());

        assertThat(userRepository.findByEmail("missing@huly.com")).isEmpty();
    }

    @Test
    void existsByEmail_shouldDelegateToJpa() {
        when(jpaRepository.existsByEmail("user@huly.com")).thenReturn(true);

        assertThat(userRepository.existsByEmail("user@huly.com")).isTrue();
    }

    @Test
    void save_shouldMapEntityFieldsAndReturnMappedDomain() {
        AppUser domain = AppUser.builder()
                .email("new@huly.com").password("encoded")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        AppUserEntity savedEntity = AppUserEntity.builder()
                .id(5L).email("new@huly.com").password("encoded")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        when(jpaRepository.save(any(AppUserEntity.class))).thenReturn(savedEntity);

        AppUser result = userRepository.save(domain);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getEmail()).isEqualTo("new@huly.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void save_shouldCreateUserDetailEntity_whenNameIsPresent() {
        AppUser domain = AppUser.builder()
                .name("Juan").email("new@huly.com").password("encoded")
                .birthDate(LocalDate.of(2000, 1, 1))
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        AppUserEntity savedEntity = AppUserEntity.builder()
                .id(5L).email("new@huly.com").build();
        when(jpaRepository.save(any(AppUserEntity.class))).thenReturn(savedEntity);
        when(userDetailRepository.save(any(UserDetailEntity.class))).thenReturn(null);

        userRepository.save(domain);

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Juan");
        assertThat(captor.getValue().getBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(captor.getValue().getAppUser()).isEqualTo(savedEntity);
    }

    @Test
    void save_shouldNotCreateUserDetailEntity_whenNameIsNull() {
        AppUser domain = AppUser.builder()
                .email("new@huly.com").password("encoded")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        AppUserEntity savedEntity = AppUserEntity.builder().id(5L).build();
        when(jpaRepository.save(any(AppUserEntity.class))).thenReturn(savedEntity);

        userRepository.save(domain);

        verify(userDetailRepository, never()).save(any());
    }

    @Test
    void save_shouldMapDomainFieldsToEntityBeforePersisting() {
        AppUser domain = AppUser.builder()
                .email("new@huly.com").password("encoded")
                .role(UserRole.USER).status(UserStatus.ACTIVE).build();
        AppUserEntity returned = AppUserEntity.builder().id(1L).email("new@huly.com")
                .password("encoded").role(UserRole.USER).status(UserStatus.ACTIVE).build();
        when(jpaRepository.save(any(AppUserEntity.class))).thenReturn(returned);

        ArgumentCaptor<AppUserEntity> captor = ArgumentCaptor.forClass(AppUserEntity.class);
        userRepository.save(domain);

        verify(jpaRepository).save(captor.capture());
        AppUserEntity captured = captor.getValue();
        assertThat(captured.getEmail()).isEqualTo("new@huly.com");
        assertThat(captured.getPassword()).isEqualTo("encoded");
        assertThat(captured.getRole()).isEqualTo(UserRole.USER);
        assertThat(captured.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void saveLeadDetail_shouldSaveUserDetailWithNicknameAndSourceAction() {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);

        userRepository.saveLeadDetail(10L, "hulyuser", SourceAction.LANDING);

        verify(userDetailRepository).save(captor.capture());
        UserDetailEntity saved = captor.getValue();
        assertThat(saved.getNickname()).isEqualTo("hulyuser");
        assertThat(saved.getSourceAction()).isEqualTo(SourceAction.LANDING);
        assertThat(saved.getAppUser().getId()).isEqualTo(10L);
    }

    @Test
    void saveLeadDetail_shouldSetCreatedAt() {
        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);

        userRepository.saveLeadDetail(1L, "hulyuser", SourceAction.GOALS);

        verify(userDetailRepository).save(captor.capture());
        assertThat(captor.getValue().getCreatedAt()).isNotNull();
    }
}
