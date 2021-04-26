package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.BadRequestException;
import com.edorogush.debijenkorftask.exception.ImageTypeNotExistException;
import com.edorogush.debijenkorftask.model.ImageType;
import org.springframework.stereotype.Service;

/**
 * The class is implementation of {@link RequestValidator} interface and provides the simple logic
 * of validation
 */
@Service
public class SimpleRequestValidator implements RequestValidator {
  private static final String ORIGINAL = "original";

  /**
   * Method to validate {@code imageTypeName} value for search requests. If validation failed,
   * {@link ImageTypeNotExistException} thrown
   *
   * @param imageTypeName name of imageType. Should be one of {@link ImageType} names.
   * @throws ImageTypeNotExistException if validation failed.
   */
  @Override
  public void checkImageTypeForSearch(String imageTypeName) {
    if (ImageType.findByName(imageTypeName) == null) {
      throw new ImageTypeNotExistException(
          String.format("Unknown predefined image type: %s .", imageTypeName));
    }
  }

  /**
   * Method to validate {@code imageTypeName} value for delete requests. If validation failed,
   * {@link ImageTypeNotExistException} thrown
   *
   * @param imageTypeName name of imageType. Should be one of {@link ImageType} names.
   * @throws ImageTypeNotExistException if validation failed.
   */
  @Override
  public void checkImageTypeForDelete(String imageTypeName) {
    if (ORIGINAL.equals(imageTypeName)) {
      return;
    }
    if (ImageType.findByName(imageTypeName) == null) {
      throw new ImageTypeNotExistException(
          String.format("Unknown predefined image type: %s .", imageTypeName));
    }
  }

  /**
   * Method to validate {@code imageName}
   *
   * @param imageName string value;
   * @throws BadRequestException if validation failed.
   */
  @Override
  public void checkImageName(String imageName) {
    if (imageName.isBlank()) {
      throw new BadRequestException(
          String.format("Invalid image name %s. Should not be null or empty.", imageName));
    }
    final int dotIndex = imageName.lastIndexOf(".");
    if (dotIndex < 0) {
      throw new BadRequestException(
          String.format("Invalid image name %s. Should contain extension.", imageName));
    }
    final String extension = imageName.substring(dotIndex + 1);
    try {
      ImageType.ImageExtension.valueOf(extension.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new BadRequestException(String.format("Invalid image extension: %s.", extension));
    }
  }
}
