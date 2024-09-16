package com.skillstorm.taxservice.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class NotFoundExceptionTest {

    @Test
    public void testDefaultConstructor() {
        // When: creating a new NotFoundException using the default constructor
        NotFoundException exception = new NotFoundException();

        // Then: The message should be null, since none was provided
        assertNull(exception.getMessage());
    }

    @Test
    public void testParameterizedConstructor() {
        // Given: A specific error message
        String message = "Resource not found";

        // When: creating a new NotFoundException with the message
        NotFoundException exception = new NotFoundException(message);

        // Then: The message should be set correctly
        assertEquals(message, exception.getMessage());
    }
}
