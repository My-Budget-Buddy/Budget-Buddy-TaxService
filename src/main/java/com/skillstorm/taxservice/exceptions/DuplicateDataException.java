package com.skillstorm.taxservice.exceptions;

import org.springframework.dao.DataIntegrityViolationException;

public class DuplicateDataException extends DataIntegrityViolationException {
    public DuplicateDataException(String message, int year) {
        super(message + " " + year);
    }

    public DuplicateDataException(String message, String name) {
        super(message + " " + name);
    }
}
