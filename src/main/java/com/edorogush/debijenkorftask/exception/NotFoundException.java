package com.edorogush.debijenkorftask.exception;

/** Exception class to represent requested data not found issues */
public class NotFoundException extends RuntimeException {
  public NotFoundException(String message) {
    super(message);
  }
}
