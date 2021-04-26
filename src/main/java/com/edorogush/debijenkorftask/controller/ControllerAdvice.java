package com.edorogush.debijenkorftask.controller;

import com.edorogush.debijenkorftask.exception.BadRequestException;
import com.edorogush.debijenkorftask.exception.ImageTypeNotExistException;
import com.edorogush.debijenkorftask.exception.NotFoundInSourceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** A centralized handler for exceptions. */
@RestControllerAdvice
public class ControllerAdvice extends ResponseEntityExceptionHandler {
  private static final Logger logger = LogManager.getLogger();

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<Object> badRequestException(BadRequestException e) {
    logger.debug(e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
  }

  @ExceptionHandler(value = {NotFoundInSourceException.class, ImageTypeNotExistException.class})
  public ResponseEntity<Object> logInfoException(RuntimeException e) {
    logger.info(e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Object> all(Exception e) {
    logger.error(e.getMessage(), e);
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
  }
}
