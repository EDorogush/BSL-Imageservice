package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.BadRequestException;
import com.edorogush.debijenkorftask.exception.ImageTypeNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests for {@link SimpleRequestValidator}. */
class SimpleRequestValidatorTest {
  private SimpleRequestValidator requestValidator;

  @BeforeEach
  void setUp() {
    requestValidator = new SimpleRequestValidator();
  }

  @Test
  void checkImageName_whenWrongExtension_thenThrowBadRequestException() {
    final String imageNameWithWrongExtension = "abc.pdf";
    assertThrows(
        BadRequestException.class,
        () -> requestValidator.checkImageName(imageNameWithWrongExtension));
  }

  @Test
  void checkImageName_whenNoExtension_thenThrowBadRequestException() {
    final String imageNameNoExtension = "abc";
    assertThrows(
        BadRequestException.class, () -> requestValidator.checkImageName(imageNameNoExtension));
  }

  @Test
  void checkImageTypeForSearch_whenNotPredefinedType_thenThrowImageTypeNotExistException() {
    final String imageTypeUnknown = "abc";
    assertThrows(
        ImageTypeNotExistException.class, () -> requestValidator.checkImageTypeForSearch(imageTypeUnknown));
  }

  @Test
  void checkImageTypeForDelete_whenOriginal_thenNoException() {
    final String imageOriginal = "original";
    assertThrows(
            ImageTypeNotExistException.class, () -> requestValidator.checkImageTypeForSearch(imageOriginal));
  }
}
