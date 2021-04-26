package com.edorogush.debijenkorftask.exception;

/**
 * Exception class to represent request validation issue when predefined image type does not exist.
 */
public class ImageTypeNotExistException extends RuntimeException {
  public ImageTypeNotExistException(String message) {
    super(message);
  }
}
