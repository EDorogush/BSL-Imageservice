package com.edorogush.debijenkorftask.exception;

/** Exception class to represent issues connected to loading images from source */
public class ImageLoaderRequestException extends RuntimeException {

  public ImageLoaderRequestException(String message) {
    super(message);
  }

  public ImageLoaderRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
