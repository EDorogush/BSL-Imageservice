package com.edorogush.debijenkorftask.exception;

/** Exception class to represent issues during image processing. */
public class ImageProcessingException extends RuntimeException {
  public ImageProcessingException(String message, Throwable cause) {
    super(message, cause);
  }
}
