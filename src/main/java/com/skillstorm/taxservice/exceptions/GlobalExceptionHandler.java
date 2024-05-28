package com.skillstorm.taxservice.exceptions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@PropertySource("classpath:SystemMessages.properties")
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    // Handle NotFoundException from querying dbs for resources that do not exist:
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Handle Bad Requests from trying to add or update entities with invalid data in the RequestBody:
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", ")));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle User trying to insert duplicate data (multiple tax returns for the same year,
    // claiming the same deduction more than once in one return, etc.):
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorMessage> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getMessage());

        return ResponseEntity.badRequest().body(error);
    }

    // Handle UnauthorizedException from trying to access resources not owned by the user::
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<String> handleUnauthorizedException(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Handle UnableToReadStreamException from errors reading from InputStream:
    @ExceptionHandler(UnableToReadStreamException.class)
    public ResponseEntity<String> handleUnableToReadStreamException(UnableToReadStreamException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Handle UndeterminedContentException from errors reading from InputStream:
    @ExceptionHandler(UndeterminedContentException.class)
    public ResponseEntity<String> handleUndeterminedContentException(UndeterminedContentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // Handle IllegalAccessError from trying to sum other income fields
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<String> handleIllegalAccessError(IllegalAccessException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
  }
}
