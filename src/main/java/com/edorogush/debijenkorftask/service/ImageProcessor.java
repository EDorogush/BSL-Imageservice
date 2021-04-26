package com.edorogush.debijenkorftask.service;

import org.springframework.core.io.Resource;

import java.util.List;

/** Represents ways of process images. */
public interface ImageProcessor {

  /**
   * Method to process image.
   *
   * @param originalImage {@link Resource} original image to process.
   * @param imageTypeName {@link String} imageType which determines the way of image processing.
   * @return {@link Resource} image after processing.
   */
  Resource processImage(Resource originalImage, String imageTypeName);

  /**
   * Method provides all imageType are allowed.
   *
   * @return List of all allowed imageTypes
   */
  List<String> imageTypes();
}
