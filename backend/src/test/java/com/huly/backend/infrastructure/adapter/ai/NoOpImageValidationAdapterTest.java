package com.huly.backend.infrastructure.adapter.ai;

import com.huly.backend.domain.exception.ImageValidationUnavailableException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoOpImageValidationAdapterTest {

    private final NoOpImageValidationAdapter adapter = new NoOpImageValidationAdapter();

    @Test
    void validate_shouldAlwaysThrowImageValidationUnavailableException() {
        assertThatThrownBy(() ->
                adapter.validate(new byte[]{1, 2, 3}, "image/jpeg", "Correr 5km", "Descripción")
        ).isInstanceOf(ImageValidationUnavailableException.class);
    }

    @Test
    void validate_shouldThrowEvenWithNullArguments() {
        assertThatThrownBy(() ->
                adapter.validate(null, null, null, null)
        ).isInstanceOf(ImageValidationUnavailableException.class);
    }
}
