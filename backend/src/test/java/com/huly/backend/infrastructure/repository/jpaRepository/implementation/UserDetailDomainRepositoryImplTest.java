package com.huly.backend.infrastructure.repository.jpaRepository.implementation;

import com.huly.backend.exception.NotFoundException;
import com.huly.backend.infrastructure.repository.entity.UserDetailEntity;
import com.huly.backend.infrastructure.repository.jpaRepository.interfaces.UserDetailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailDomainRepositoryImplTest {

    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private UserDetailDomainRepositoryImpl userDetailDomainRepository;

    @Test
    void findProfileOnBoardingCompleted_shouldReturnValue_whenUserDetailExists() {
        UserDetailEntity entity = UserDetailEntity.builder()
                .id(1L).profileOnBoardingCompleted(true).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        Optional<Boolean> result = userDetailDomainRepository.findProfileOnBoardingCompleted(1L);

        assertThat(result).isPresent();
        assertThat(result.get()).isTrue();
    }

    @Test
    void findProfileOnBoardingCompleted_shouldReturnEmpty_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        Optional<Boolean> result = userDetailDomainRepository.findProfileOnBoardingCompleted(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void completeOnboarding_shouldSetAnswersAndMarkCompleted() {
        UserDetailEntity entity = UserDetailEntity.builder().id(1L).build();
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(entity));

        userDetailDomainRepository.completeOnboarding(1L, "Calmar mi mente", "Soltar el control",
                "Respirar antes de reaccionar");

        ArgumentCaptor<UserDetailEntity> captor = ArgumentCaptor.forClass(UserDetailEntity.class);
        verify(userDetailRepository).save(captor.capture());
        UserDetailEntity saved = captor.getValue();
        assertThat(saved.getOnboardingAnswer1()).isEqualTo("Calmar mi mente");
        assertThat(saved.getOnboardingAnswer2()).isEqualTo("Soltar el control");
        assertThat(saved.getOnboardingAnswer3()).isEqualTo("Respirar antes de reaccionar");
        assertThat(saved.getProfileOnBoardingCompleted()).isTrue();
    }

    @Test
    void completeOnboarding_shouldThrowNotFoundException_whenUserDetailNotFound() {
        when(userDetailRepository.findFirstByAppUser_IdOrderByCreatedAtDesc(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailDomainRepository.completeOnboarding(99L, "A", "B", "C"))
                .isInstanceOf(NotFoundException.class);
    }

}
