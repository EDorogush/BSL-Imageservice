package com.edorogush.debijenkorftask.exception;

/** Exception class to represent request validation issues */
public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
