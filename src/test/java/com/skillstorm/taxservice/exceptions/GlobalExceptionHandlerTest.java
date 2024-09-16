package com.skillstorm.taxservice.exceptions;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

public class GlobalExceptionHandlerTest {

    private AutoCloseable closeable;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    // Test for NotFoundException handler
    @Test
    public void testHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("Resource not found");
        ResponseEntity<String> response = globalExceptionHandler.handleNotFoundException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Resource not found", response.getBody());
    }

    // Test for MethodArgumentNotValidException handler
    @Test
    public void testHandleValidationExceptions() {
        FieldError fieldError = new FieldError("object", "field", "Invalid field value");
        BindingResult bindingResult = new BindException("target", "object");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getErrorCode());
        assertEquals("Invalid field value", response.getBody().getMessage());
    }

    // Test for DataIntegrityViolationException handler
    @Test
    public void testHandleDataIntegrityViolationException() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Duplicate entry");
        ResponseEntity<ErrorMessage> response = globalExceptionHandler.handleDataIntegrityViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getErrorCode());
        assertEquals("Duplicate entry", response.getBody().getMessage());
    }

    // Test for UnauthorizedException handler
    @Test
    public void testHandleUnauthorizedException() {
        UnauthorizedException ex = new UnauthorizedException("Access denied");
        ResponseEntity<String> response = globalExceptionHandler.handleUnauthorizedException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access denied", response.getBody());
    }

    // Test for IllegalArgumentException handler
    @Test
    public void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalArgumentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid argument", response.getBody());
    }

    // Test for UnableToReadStreamException handler
    @Test
    public void testHandleUnableToReadStreamException() {
        UnableToReadStreamException ex = new UnableToReadStreamException("Unable to read stream");
        ResponseEntity<String> response = globalExceptionHandler.handleUnableToReadStreamException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unable to read stream", response.getBody());
    }

    // Test for UndeterminedContentException handler
    @Test
    public void testHandleUndeterminedContentException() {
        UndeterminedContentException ex = new UndeterminedContentException("Undetermined content");
        ResponseEntity<String> response = globalExceptionHandler.handleUndeterminedContentException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Undetermined content", response.getBody());
    }

    // Test for IllegalAccessException handler
    @Test
    public void testHandleIllegalAccessError() {
        IllegalAccessException ex = new IllegalAccessException("Illegal access");
        ResponseEntity<String> response = globalExceptionHandler.handleIllegalAccessError(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Illegal access", response.getBody());
    }
}
