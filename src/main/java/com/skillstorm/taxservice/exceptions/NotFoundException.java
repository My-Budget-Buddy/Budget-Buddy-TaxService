package com.skillstorm.taxservice.exceptions;

public class NotFoundException extends RuntimeException {
  public NotFoundException() {
    super();
  }

  public NotFoundException(String message) {
    super(message);
  }

  public NotFoundException(String message, int id) {
    this(message + " " + id);
  }
}
