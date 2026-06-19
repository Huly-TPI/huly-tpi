package com.huly.backend.domain.useCase.badge;
import com.huly.backend.domain.model.badge.Badge;
import com.huly.backend.domain.repository.badge.BadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllBadgesUseCaseTest {

    @Mock
    private BadgeRepository badgeRepository;
    @InjectMocks
    private GetAllBadgesUseCase getAllBadgesUseCase;

    @Test
    void execute_shouldReturnAllBadges() { 
        List<Badge> badges = List.of(
                Badge.builder().id(1L).code("PRIMER_PASO").name("Primer paso").build(),
                Badge.builder().id(2L).code("VALENTÍA").name("Valentía").build()
        );
        when(badgeRepository.findAll()).thenReturn(badges);
        List<Badge> result = getAllBadgesUseCase.execute();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCode()).isEqualTo("PRIMER_PASO");
        assertThat(result.get(1).getCode()).isEqualTo("VALENTÍA");
        verify(badgeRepository).findAll();
    }

    @Test
    void execute_shouldReturnEmptyList_whenNoBadgesExist() {
        when(badgeRepository.findAll()).thenReturn(List.of());
        List<Badge> result = getAllBadgesUseCase.execute();
        assertThat(result).isEmpty();
        verify(badgeRepository).findAll();
    }
    
}
