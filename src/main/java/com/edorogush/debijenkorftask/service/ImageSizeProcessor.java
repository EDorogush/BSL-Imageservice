package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.ImageProcessingException;
import com.edorogush.debijenkorftask.model.ImageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class is implementation of {@link ImageProcessor} interface and provides the logic of
 * resizing images.
 */
@Service
public class ImageSizeProcessor implements ImageProcessor {
  private static final Logger logger = LogManager.getLogger();
  private final List<String> imageTypes =
      Arrays.stream(ImageType.values()).map(ImageType::getName).collect(Collectors.toList());

  @Override
  public List<String> imageTypes() {
    return imageTypes;
  }

  @Override
  public Resource processImage(Resource originalImage, String imageTypeName) {
    ImageType imageType = ImageType.findByName(imageTypeName);
    if (imageType == null) {
      logger.debug(
          "No predefined imageType found for {}. No image resizing provided", imageTypeName);
      return originalImage;
    }
    try (InputStream originalImageStream = originalImage.getInputStream();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
      final BufferedImage imageToResize = ImageIO.read(originalImageStream);
      final BufferedImage imageResized = resize(imageToResize, imageType);
      ImageIO.write(imageResized, imageType.getImageExtension().name(), outStream);
      return new ByteArrayResource(outStream.toByteArray());
    } catch (IOException e) {
      throw new ImageProcessingException("Failed to resize image.", e);
    }
  }

  private BufferedImage resize(BufferedImage imageToProcess, ImageType imageType) {
    ImageType.ScaleType scaleType = imageType.getScaleType();
    switch (scaleType) {
      case CROP:
        return cropImage(imageToProcess, imageType);
      case FILL:
        return fillImage(imageToProcess, imageType);
      case SKEW:
        return skewImage(imageToProcess, imageType);
      default:
        logger.debug(
            "No processor for {} rule provided. Image does not changed.", imageType.name());
        return imageToProcess;
    }
  }

  /**
   * Method to crop the image to new width and height specified by {@code imageType} parameter.
   * Image parts that no longer fit the new aspect ratio are cut.
   */
  private BufferedImage cropImage(BufferedImage image, ImageType imageType) {
    BufferedImage imageCropped = initEmptyImage(imageType);
    Graphics2D graph = imageCropped.createGraphics();
    graph.drawImage(image, 0, 0, Color.WHITE, null);
    graph.dispose();
    return imageCropped;
  }

  private BufferedImage initEmptyImage(ImageType imageType) {
    return new BufferedImage(
        imageType.getWidthPx(), imageType.getHeightPx(), BufferedImage.TYPE_INT_RGB);
  }

  /**
   * Method to fill up the parts of the image that no longer fit the new aspect ration with a
   * background-color specified by {@code imageType} parameter.
   */
  private BufferedImage fillImage(BufferedImage image, ImageType imageType) {
    final Color bgColor = Color.decode(imageType.getFillColorHexValue());
    BufferedImage imageFilled = initEmptyImage(imageType);
    // draw background
    Graphics2D graph = imageFilled.createGraphics();
    graph.setColor(bgColor);
    graph.fill(new Rectangle(imageFilled.getWidth(), imageFilled.getHeight()));
    graph.dispose();
    // draw image over the background
    graph = imageFilled.createGraphics();
    graph.drawImage(image, 0, 0, bgColor, null);
    graph.dispose();
    return imageFilled;
  }

  /**
   * Method to simply squeeze the image to fit the new height and width specified by {@code
   * imageType} parameter.
   */
  private BufferedImage skewImage(BufferedImage image, ImageType imageType) {
    Image scaledInstance =
        image.getScaledInstance(
            imageType.getWidthPx(), imageType.getHeightPx(), Image.SCALE_DEFAULT);
    return toBufferedImage(scaledInstance);
  }

  private BufferedImage toBufferedImage(Image img) {
    if (img instanceof BufferedImage) {
      return (BufferedImage) img;
    }
    BufferedImage bImage =
        new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    Graphics2D bGr = bImage.createGraphics();
    bGr.drawImage(img, 0, 0, null);
    bGr.dispose();
    return bImage;
  }
}
