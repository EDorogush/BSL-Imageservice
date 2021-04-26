package com.edorogush.debijenkorftask.model;

import org.springframework.http.MediaType;

import java.awt.*;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enumeration of predefined image's types. Each type contains set of properties which are used to
 * optimize image.
 */
public enum ImageType {
  THUMBNAIL("thumbnail", 500, 1000, 10, ScaleType.CROP, null, ImageExtension.JPG),
  TECH_DRAWING("technical-drawing", 1000, 1000, 10, ScaleType.FILL, "#00ff00", ImageExtension.PNG),
  ICON("icon", 200, 200, 10, ScaleType.SKEW, null, ImageExtension.PNG);

  private final String name;
  private final int heightPx;

  private final int widthPx;
  private final int quality;
  private final ScaleType scaleType;
  private final String fillColorHexValue;
  private final ImageExtension imageExtension;

  private static final Map<String, ImageType> map =
      Arrays.stream(ImageType.values())
          .collect(Collectors.toMap(ImageType::getName, Function.identity()));

  ImageType(
      String name,
      int heightPx,
      int width,
      int quality,
      ScaleType scaleType,
      String fillColorHexValue,
      ImageExtension imageExtension) {
    validateScaleType(scaleType, fillColorHexValue);
    validateQuality(quality);
    this.name = name;
    this.imageExtension = imageExtension;
    this.heightPx = heightPx;
    this.widthPx = width;
    this.quality = quality;
    this.scaleType = scaleType;
    this.fillColorHexValue = fillColorHexValue;
  }

  public String getName() {
    return name;
  }

  public int getHeightPx() {
    return heightPx;
  }

  public int getWidthPx() {
    return widthPx;
  }

  public int getQuality() {
    return quality;
  }

  public ScaleType getScaleType() {
    return scaleType;
  }

  public String getFillColorHexValue() {
    return fillColorHexValue;
  }

  public ImageExtension getImageExtension() {
    return imageExtension;
  }

  public static ImageType findByName(String name) {
    return map.get(name);
  }

  private void validateQuality(int quality) {
    if (quality < 0 || quality > 100) {
      throw new IllegalArgumentException("Wrong quality value. Must be in range 0 - 100.");
    }
  }

  private void validateScaleType(ScaleType scaleType, String fillColorHexValue) {
    if (scaleType != ScaleType.FILL && fillColorHexValue != null) {
      throw new IllegalArgumentException(
          "FillColorHexValue parameter is for ScaleType1.FILL only.");
    }
    if (scaleType == ScaleType.FILL) {
      try {
        Color.decode(fillColorHexValue);
      } catch (Exception e) {
        throw new IllegalArgumentException("Wrong fillColorHexValue.");
      }
    }
  }

  /** Enumeration of possible ways to optimize image. */
  public enum ScaleType {
    CROP,
    FILL,
    SKEW
  }

  /** Enumeration of file extensions that can be processed in application. */
  public enum ImageExtension {
    JPG(MediaType.IMAGE_JPEG),
    JPEG(MediaType.IMAGE_JPEG),
    PNG(MediaType.IMAGE_PNG);

    private final MediaType mediaType;

    ImageExtension(MediaType mediaType) {
      this.mediaType = mediaType;
    }

    public MediaType getMediaType() {
      return mediaType;
    }
  }
}
