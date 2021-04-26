package com.edorogush.debijenkorftask.service;

/** Represents client's request data validation */
public interface RequestValidator {

  void checkImageTypeForSearch(String imageType);

  void checkImageTypeForDelete(String imageType);

  void checkImageName(String imageName);
}
