package com.skillstorm.taxservice.exceptions;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.Arrays;
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
    public ResponseEntity<ErrorMessage> handleNotFoundException(NotFoundException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // Handle Bad Requests from trying to add or update entities with invalid data in the RequestBody:
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(MethodArgumentNotValidException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage).collect(Collectors.joining(", ")));

        return ResponseEntity.badRequest().body(error);
    }

    // Handle Constraint Violation Exceptions that occur from methods updating entities with invalid data:
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorMessage> handleConstraintViolationExceptions(ConstraintViolationException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // Handle Bad Requests from trying to add or update entities with invalid data in the RequestBody:
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorMessage> handleValidationExceptions(HandlerMethodValidationException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(Arrays.stream(e.getDetailMessageArguments()).map(Object::toString).collect(Collectors.joining(", ")));

        return ResponseEntity.badRequest().body(error);
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
    public ResponseEntity<ErrorMessage> handleUnauthorizedException(UnauthorizedException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.FORBIDDEN.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    // Handle UnauthorizedException from trying to access S3 without proper credentials:
    // This is probably more of a 500 error because it would be a configuration issue:
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorMessage> handleS3Exception(S3Exception e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.FORBIDDEN.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // Handle UnableToReadStreamException from errors reading from InputStream:
    @ExceptionHandler(UnableToReadStreamException.class)
    public ResponseEntity<ErrorMessage> handleUnableToReadStreamException(UnableToReadStreamException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // Handle UndeterminedContentException from errors reading from InputStream:
    @ExceptionHandler(UndeterminedContentException.class)
    public ResponseEntity<ErrorMessage> handleUndeterminedContentException(UndeterminedContentException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    // Handle IllegalAccessError from trying to sum other income fields:
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<ErrorMessage> handleIllegalAccessError(IllegalAccessException e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(error.getMessage());
      return ResponseEntity.badRequest().body(error);
  }

    // Everything else:
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleGeneralExceptions(Exception e) {
        ErrorMessage error = new ErrorMessage();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(e.getMessage());
        return ResponseEntity.internalServerError().body(error);
    }
}
