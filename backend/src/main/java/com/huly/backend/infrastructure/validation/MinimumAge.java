package com.huly.backend.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MinimumAgeValidator.class)
public @interface MinimumAge {

    int value();

    String message() default "Edad mínima inválida";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}