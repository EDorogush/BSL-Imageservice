package com.edorogush.debijenkorftask.service;

import com.edorogush.debijenkorftask.exception.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Service to manage request processing for searching and deleting images. */
@Service
public class ImageService {
  private static final Logger logger = LogManager.getLogger();
  private static final String ORIGINAL = "original";
  private final FileStorage fileStorage;
  private final ImageProcessor imageProcessor;
  private final ImageLoader imageLoader;

  public ImageService(
      FileStorage fileStorage, ImageProcessor imageProcessor, ImageLoader imageLoader) {
    this.fileStorage = fileStorage;
    this.imageProcessor = imageProcessor;
    this.imageLoader = imageLoader;
  }

  /**
   * Method to search image with {@code fileName} with predefined {@code imageTypeName} in storage.
   * At first, searching in {@link FileStorage} within {@code imageTypeName} directory provided. If
   * no such image found then attempt to search the same file in "original" directory provided. If
   * still file not found an attempt to load image from {@link ImageLoader} provided. After image
   * successfully found it is added to "original" directory in {@link FileStorage} and also
   * processed by {@link ImageProcessor} and new image is loaded to {@code imageTypeName} directory.
   *
   * @param fileName Image's name with extension.
   * @param imageTypeName name of predefined type which defines the way of image processing vis
   *     {@link ImageProcessor} and also it is the name of directory in {@link FileStorage} where
   *     image search is provided.
   * @return {@link Resource} Image data.
   */
  public Resource findByName(String fileName, String imageTypeName) {
    final String fileNameWithoutSlashes = replaceSlash(fileName);
    final String locationInTypeDir = StorageLocationResolver.resolve(fileNameWithoutSlashes);
    final String imageProcessedLocation = imageTypeName + locationInTypeDir;
    // try to find in imageTypeName dir
    try {
      return fileStorage.get(imageProcessedLocation + fileNameWithoutSlashes);
    } catch (NotFoundException e) {
      logger.info(
          "No image {} in FileStorage found",
          () -> (imageProcessedLocation + fileNameWithoutSlashes));
    }
    // try to find in "original" dir
    final String imageOriginalLocation = ORIGINAL + locationInTypeDir;
    try {
      final Resource imageOriginal =
          fileStorage.get(imageOriginalLocation + fileNameWithoutSlashes);
      final Resource imageProcessed = imageProcessor.processImage(imageOriginal, imageTypeName);
      fileStorage.put(imageProcessed, imageProcessedLocation + fileNameWithoutSlashes);
      return imageProcessed;
    } catch (NotFoundException e) {
      logger.info(
          "No original image {} in FileStorage found",
          () -> (imageOriginalLocation + fileNameWithoutSlashes));
    }
    // try to load from source
    final Resource imageOriginal = imageLoader.getImageFromSource(fileName);
    fileStorage.put(imageOriginal, imageOriginalLocation + fileNameWithoutSlashes);
    final Resource imageProcessed = imageProcessor.processImage(imageOriginal, imageTypeName);
    fileStorage.put(imageProcessed, imageProcessedLocation + fileNameWithoutSlashes);
    return imageProcessed;
  }

  /**
   * Method to delete image/images from storage.
   *
   * @param fileName Image's name with extension.
   * @param imageType name of directory from which file should be deleted. If {@code imageType} is
   *     "original" then all * replicas of file is deleted.
   */
  public void deleteImage(String fileName, String imageType) {
    final String fileNameWithoutSlashes = replaceSlash(fileName);
    final String locationInTypeDir = StorageLocationResolver.resolve(fileNameWithoutSlashes);
    if (!imageType.equals(ORIGINAL)) {
      fileStorage.deleteOne(imageType + locationInTypeDir + fileNameWithoutSlashes);
    } else {
      List<String> fileNameList = collectAllReplicas(fileNameWithoutSlashes, locationInTypeDir);
      fileNameList.add(ORIGINAL + locationInTypeDir + fileNameWithoutSlashes);
      fileStorage.deleteMany(fileNameList);
    }
  }

  private List<String> collectAllReplicas(String fileNameWithoutSlashes, String locationInStorage) {
    return imageProcessor.imageTypes().stream()
        .map(rule -> rule + locationInStorage + fileNameWithoutSlashes)
        .collect(Collectors.toList());
  }

  private String replaceSlash(String fileName) {
    return fileName.replaceAll("/", "_");
  }
}
