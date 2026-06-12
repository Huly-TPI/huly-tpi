package com.huly.backend.domain.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRuleExceptionTest {

    @Test
    void testMessageIsSet() {
        BusinessRuleException ex = new BusinessRuleException("source es obligatorio");
        assertEquals("source es obligatorio", ex.getMessage());
    }

    @Test
    void testExtendsDomainException() {
        BusinessRuleException ex = new BusinessRuleException("test");
        assertInstanceOf(DomainException.class, ex);
    }

    @Test
    void testNullMessage() {
        BusinessRuleException ex = new BusinessRuleException(null);
        assertNull(ex.getMessage());
    }
}
