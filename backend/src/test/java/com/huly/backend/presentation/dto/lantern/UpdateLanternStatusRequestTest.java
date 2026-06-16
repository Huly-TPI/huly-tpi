package com.huly.backend.presentation.dto.lantern;

import com.huly.backend.infrastructure.presentation.dto.lantern.UpdateLanternStatusRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateLanternStatusRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidation_whenStatusIsProvided() {
        UpdateLanternStatusRequest request = new UpdateLanternStatusRequest("COMPLETED");
        Set<ConstraintViolation<UpdateLanternStatusRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenStatusIsNull() {
        UpdateLanternStatusRequest request = new UpdateLanternStatusRequest(null);
        Set<ConstraintViolation<UpdateLanternStatusRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("status"));
    }

    @Test
    void shouldExposeStatusField() {
        UpdateLanternStatusRequest request = new UpdateLanternStatusRequest("CANCELLED");
        assertThat(request.status()).isEqualTo("CANCELLED");
    }
}
