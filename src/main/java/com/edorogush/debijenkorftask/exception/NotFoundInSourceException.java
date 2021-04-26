package com.edorogush.debijenkorftask.exception;

/** Exception class to represent Not Found error during request to image source server. */
public class NotFoundInSourceException extends RuntimeException {
  public NotFoundInSourceException(String message) {
    super(message);
  }
}
