package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessRuleExceptionTest {

    @Test
    void messageConstructor_shouldSetMessage() {
        BusinessRuleException ex = new BusinessRuleException("source es obligatorio");
        assertThat(ex.getMessage()).isEqualTo("source es obligatorio");
    }

    @Test
    void inheritance_shouldExtendDomainException() {
        BusinessRuleException ex = new BusinessRuleException("test");
        assertThat(ex).isInstanceOf(DomainException.class);
    }

    @Test
    void messageConstructor_shouldHandleNullMessage() {
        BusinessRuleException ex = new BusinessRuleException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
