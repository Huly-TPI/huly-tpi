package com.huly.backend.exception;

import com.huly.backend.infrastructure.presentation.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

class NotFoundExceptionTest {

    @Test
    void testNotFoundExceptionWithMessage() {
        String message = "Not found error";
        NotFoundException exception = new NotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testNotFoundExceptionWithResourceFieldAndValue() {
        NotFoundException exception = new NotFoundException("Usuario", "ID", 123);

        assertEquals("No se encontro un Usuario con ID 123", exception.getMessage());
    }

    @Test
    void testNotFoundExceptionWithStringValue() {
        NotFoundException exception = new NotFoundException("Producto", "nombre", "test");

        assertEquals("No se encontro un Producto con nombre test", exception.getMessage());
    }

    @Test
    void testResponseStatusAnnotation() {
        ResponseStatus annotation = NotFoundException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation);
        assertEquals(HttpStatus.NOT_FOUND, annotation.value());
    }

    @Test
    void testNotFoundExceptionIsRuntimeException() {
        NotFoundException exception = new NotFoundException("test");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void testNotFoundExceptionWithNullValues() {
        NotFoundException exception = new NotFoundException(null, null, null);
        assertEquals("No se encontro un null con null null", exception.getMessage());
    }

    @Test
    void testNotFoundExceptionWithEmptyValues() {
        NotFoundException exception = new NotFoundException("", "", "");
        assertEquals("No se encontro un  con  ", exception.getMessage());
    }
}
