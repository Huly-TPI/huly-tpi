package com.huly.backend.domain.useCase.admin.activities;

import com.huly.backend.domain.model.activity.Activity;
import com.huly.backend.domain.repository.activity.ActivityRepository;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAdminActivitiesUseCaseTest {

    @Test
    void execute_shouldReturnAllActivities() {
        ActivityRepository repository = mock(ActivityRepository.class);
        GetAdminActivitiesUseCase useCase = new GetAdminActivitiesUseCase(repository);

        Activity act1 = Activity.builder().id(1L).title("Act1").build();
        Activity act2 = Activity.builder().id(2L).title("Act2").build();
        when(repository.findAll()).thenReturn(List.of(act1, act2));

        List<Activity> result = useCase.execute();

        assertThat(result).hasSize(2).containsExactly(act1, act2);
    }
}
