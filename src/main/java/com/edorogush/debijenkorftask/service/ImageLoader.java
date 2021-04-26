package com.edorogush.debijenkorftask.service;

import org.springframework.core.io.Resource;

/** Represents ways to load image from selected Source. */
public interface ImageLoader {

  /**
   * Method to read image from source.
   *
   * @param imageId - unique image id.
   * @return {@link Resource}
   */
  Resource getImageFromSource(String imageId);
}
