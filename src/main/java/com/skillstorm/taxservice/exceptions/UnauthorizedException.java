package com.skillstorm.taxservice.exceptions;

public class UnauthorizedException extends IllegalArgumentException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
