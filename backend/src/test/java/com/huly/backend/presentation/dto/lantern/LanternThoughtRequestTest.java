package com.huly.backend.presentation.dto.lantern;

import com.huly.backend.infrastructure.presentation.dto.lantern.LanternThoughtRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LanternThoughtRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldPassValidation_whenTextIsValid() {
        LanternThoughtRequest request = new LanternThoughtRequest("pensamiento válido");
        Set<ConstraintViolation<LanternThoughtRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldFailValidation_whenTextIsBlank() {
        LanternThoughtRequest request = new LanternThoughtRequest("   ");
        Set<ConstraintViolation<LanternThoughtRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("text"));
    }

    @Test
    void shouldFailValidation_whenTextIsEmpty() {
        LanternThoughtRequest request = new LanternThoughtRequest("");
        Set<ConstraintViolation<LanternThoughtRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void shouldFailValidation_whenTextExceeds100Characters() {
        LanternThoughtRequest request = new LanternThoughtRequest("a".repeat(101));
        Set<ConstraintViolation<LanternThoughtRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("text"));
    }

    @Test
    void shouldPassValidation_whenTextIsExactly100Characters() {
        LanternThoughtRequest request = new LanternThoughtRequest("a".repeat(100));
        Set<ConstraintViolation<LanternThoughtRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldExposeTextField() {
        LanternThoughtRequest request = new LanternThoughtRequest("mi pensamiento");
        assertThat(request.text()).isEqualTo("mi pensamiento");
    }
}
